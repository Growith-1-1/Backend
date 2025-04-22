package dev.book.accountbook.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.book.accountbook.dto.request.CreateConnectedIdRequest;
import dev.book.accountbook.entity.Codef;
import dev.book.accountbook.exception.codef.CodefErrorCode;
import dev.book.accountbook.exception.codef.CodefErrorException;
import dev.book.accountbook.repository.CodefRepository;
import dev.book.global.util.RsaEncryptUtil;
import dev.book.user.entity.UserEntity;
import dev.book.user.exception.UserErrorCode;
import dev.book.user.exception.UserErrorException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class CodefService {

    @Value("${codef.client_id}")
    private String CLIENT_ID;
    @Value("${codef.client_secret}")
    private String CLIENT_SECRET;
    @Value("${codef.access_token}")
    private String ACCESS_TOKEN;
    private final String TOKEN_URL = "https://oauth.codef.io/oauth/token";
    private final String createConnectedId = "https://development.codef.io/v1/account/create";
    private final String transactions = "https://development.codef.io/v1/kr/bank/p/account/transaction-list";

    private final CodefRepository codefRepository;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final RsaEncryptUtil rsaEncryptUtil;

    public String getAccessToken() {
        try {
            HttpEntity<String> entity = getTokenRequest();
            ResponseEntity<String> response = restTemplate.postForEntity(TOKEN_URL, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                HashMap<String, Object> responseMap = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

                return (String) responseMap.get("access_token");
            } else {
                throw new RuntimeException("토큰 발급 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("토큰 발급 중 오류 발생", e);
        }
    }

    public boolean createConnectedId(UserEntity user, CreateConnectedIdRequest createRequest) {
        HttpEntity<Map<String, Object>> request = createConnectRequest(createRequest);
        ResponseEntity<String> response = restTemplate.postForEntity(createConnectedId, request, String.class);
        String decodeResponse = URLDecoder.decode(response.getBody(), StandardCharsets.UTF_8);
        String connectedId;
        String code;

        try {
            code = objectMapper.readTree(decodeResponse).path("result").path("code").asText();
            connectedId = objectMapper.readTree(decodeResponse).path("data").path("connectedId").asText();
        } catch (Exception e) {
            throw new CodefErrorException(CodefErrorCode.UNKNOWN_ERROR);
        }

        validationCode(code);

        codefRepository.save(createRequest.toEntity(user, createRequest.bank().getCode(), createRequest.accountNumber(), connectedId));

        return true;
    }

    public String getTransactions(UserEntity user) {
        HttpEntity<Map<String, Object>> request = createTransRequest(user);
        ResponseEntity<String> response = restTemplate.postForEntity(transactions, request, String.class);

        return URLDecoder.decode(response.getBody(), StandardCharsets.UTF_8);
    }

    private HttpEntity<String> getTokenRequest() {
        String auth = CLIENT_ID + ":" + CLIENT_SECRET;
        String encodedAuth = Base64.encodeBase64String(auth.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedAuth);

        String body = "grant_type=client_credentials&scope=read";

        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Map<String, Object>> createConnectRequest(CreateConnectedIdRequest createRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(ACCESS_TOKEN);

        String password = rsaEncryptUtil.encrypt(createRequest.password());

        Map<String, Object> body = new HashMap<>();
        body.put("countryCode", "KR");
        body.put("businessType", "BK");
        body.put("clientType", "P");
        body.put("organization", createRequest.bank().getCode());
        body.put("loginType", "1");
        body.put("id", createRequest.id());
        body.put("password", password);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("accountList", List.of(body));

        return new HttpEntity<>(requestBody, headers);
    }

    private HttpEntity<Map<String, Object>> createTransRequest(UserEntity user) {
        Codef codef = codefRepository.findByUser(user).orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(ACCESS_TOKEN);

        Map<String, Object> body = new HashMap<>();
        body.put("organization", codef.getBankCode());
        body.put("connectedId", codef.getConnectedId());
        body.put("account", codef.getAccount());
        body.put("startDate", today);
        body.put("endDate", today);
        body.put("orderBy", "0");

        return new HttpEntity<>(body, headers);
    }

    private void validationCode(String code) {
        switch (code) {
            case "CF-00000" -> {
                return;
            }
            case "CF-12801", "CF-12803" -> throw new CodefErrorException(CodefErrorCode.INVALID_LOGIN_INFO);
            case "CF-12802" -> throw new CodefErrorException(CodefErrorCode.PASSWORD_ERROR_COUNT_EXCEEDED);
            default -> throw new CodefErrorException(CodefErrorCode.UNKNOWN_ERROR, code);
        }
    }
}
