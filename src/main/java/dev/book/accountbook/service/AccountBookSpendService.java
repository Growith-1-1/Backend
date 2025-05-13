package dev.book.accountbook.service;

import dev.book.accountbook.dto.request.AccountBookMonthRequest;
import dev.book.accountbook.dto.request.AccountBookRequest;
import dev.book.accountbook.dto.request.AccountBookSpendListRequest;
import dev.book.accountbook.dto.request.AccountBookSpendRequest;
import dev.book.accountbook.dto.response.*;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.entity.Budget;
import dev.book.accountbook.exception.accountbook.AccountBookErrorCode;
import dev.book.accountbook.exception.accountbook.AccountBookErrorException;
import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.accountbook.repository.BudgetRepository;
import dev.book.accountbook.repository.TempAccountBookRepository;
import dev.book.accountbook.type.BudgetLimit;
import dev.book.accountbook.type.CategoryType;
import dev.book.achievement.achievement_user.dto.event.GetWarningBudgetEvent;
import dev.book.challenge.rank.SpendCreatedRankingEvent;
import dev.book.global.config.Firebase.dto.LimitWarningFcmEvent;
import dev.book.global.entity.Category;
import dev.book.global.repository.CategoryRepository;
import dev.book.user.entity.UserEntity;
import dev.book.user.exception.UserErrorCode;
import dev.book.user.exception.UserErrorException;
import dev.book.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountBookSpendService {
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final AccountBookRepository accountBookRepository;
    private final TempAccountBookRepository tempAccountBookRepository;

    private final int PAGE_SIZE = 10;

    private final ApplicationEventPublisher publisher;

    @Transactional(readOnly = true)
    public AccountBookResponse getSpendOne(Long id, Long userId) {
        isExistsUser(userId);
        AccountBook accountBook = findAccountBookOrThrow(id, AccountBookErrorCode.NOT_FOUND_SPEND);

        return AccountBookResponse.from(accountBook);
    }

    @Transactional(readOnly = true)
    public AccountBookListResponse getSpendList(Long userId, int page) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
        Page<AccountBook> accountBooks = accountBookRepository.findByAccountBookWithPage(userId, CategoryType.SPEND, pageable);

        List<AccountBookResponse> accountBookSpendResponseList = accountBooks.getContent().stream()
                .map(AccountBookResponse::from)
                .toList();

        return new AccountBookListResponse(
                accountBookSpendResponseList,
                accountBooks.getTotalPages(),
                accountBooks.getTotalElements(),
                accountBooks.getNumber() + 1
        );
    }

    @Transactional
    public AccountBookResponse createSpend(AccountBookSpendRequest request, UserEntity user) {
        Category category = getCategory(request.category());
        AccountBook accountBook = request.toEntity(user, category);
        AccountBook saved = accountBookRepository.save(accountBook);

        handleBudgetLimitAlert(user);

        publisher.publishEvent(new SpendCreatedRankingEvent(accountBook));

        return AccountBookResponse.from(saved);
    }

    @Transactional
    public AccountBookResponse modifySpend(AccountBookSpendRequest request, Long id, Long userId) {
        AccountBook accountBook = findAccountBookOrThrow(id, AccountBookErrorCode.NOT_FOUND_SPEND);
        updateAccountBook(accountBook, request);
        accountBookRepository.flush();

        return AccountBookResponse.from(accountBook);
    }

    @Transactional
    public boolean deleteSpend(Long id, Long userId) {
        AccountBook accountBook = findAccountBookOrThrow(id, AccountBookErrorCode.NOT_FOUND_SPEND);
        accountBookRepository.delete(accountBook);

        return true;
    }

    @Transactional(readOnly = true)
    public AccountBookListResponse getCategorySpendList(String category, Long userId, int page) {
        Category findCategory = getCategory(category);
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
        Page<AccountBook> categorySpendList = accountBookRepository.findByUserIdAndCategoryNameWithGraph(userId, findCategory.getId(), pageable);

        List<AccountBookResponse> accountBookSpendResponseList = categorySpendList.getContent().stream()
                .map(AccountBookResponse::from)
                .toList();

        return new AccountBookListResponse(
                accountBookSpendResponseList,
                categorySpendList.getTotalPages(),
                categorySpendList.getTotalElements(),
                categorySpendList.getNumber() + 1
        );
    }

    @Transactional
    public List<AccountBookResponse> createSpendList(UserEntity user, AccountBookSpendListRequest requestList) {
        List<AccountBook> accountBookList = createAccountBookList(user, requestList);
        List<AccountBook> savedAccountBookList = accountBookRepository.saveAll(accountBookList);
        tempAccountBookRepository.deleteAllByUser(user);

        return savedAccountBookList.stream()
                .map(AccountBookResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountBookPeriodListResponse getAccountBookPeriod(Long userId, LocalDate startDate, LocalDate endDate, int page) {
        isExistsUser(userId);
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
        Page<AccountBook> accountBookList = accountBookRepository.findAllPeriod(userId, startDate, endDate, pageable);

        List<AccountBookPeriodResponse> accountBookPeriodResponseList = accountBookList.getContent().stream()
                .map(AccountBookPeriodResponse::from)
                .toList();

        return new AccountBookPeriodListResponse(
                accountBookPeriodResponseList,
                accountBookList.getTotalPages(),
                accountBookList.getTotalElements(),
                accountBookList.getNumber() + 1
        );
    }

    @Transactional(readOnly = true)
    public List<AccountBookMonthResponse> getMonthAccountBook(Long userId, AccountBookMonthRequest request) {
        isExistsUser(userId);

        LocalDate startDate = request.requestMonth();
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<AccountBook> findList = accountBookRepository.findAllMonth(userId, startDate, endDate);

        return monthAccountBookList(findList, startDate, endDate);
    }

    private void isExistsUser(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new UserErrorException(UserErrorCode.USER_NOT_FOUND);
        }
    }

    private AccountBook findAccountBookOrThrow(Long id, AccountBookErrorCode errorCode) {

        return accountBookRepository.findById(id)
                .orElseThrow(() -> new AccountBookErrorException(errorCode));
    }

    private void updateAccountBook(AccountBook accountBook, AccountBookRequest request) {
        Category category = getCategory(request.category());

        accountBook.modifyTitle(request.title());
        accountBook.modifyAmount(request.amount());
        accountBook.modifyMemo(request.memo());
        accountBook.modifyEndDate(request.endDate());
        accountBook.modifyCategory(category);
        accountBook.modifyOccurredAt(request.occurredAt());

        if (request.repeat() != null) {
            accountBook.modifyFrequency(request.repeat().frequency());
            accountBook.modifyMonth(request.repeat().month());
            accountBook.modifyDay(request.repeat().day());
        }
    }

    private Category getCategory(String category) {
        String getCategory = Objects.requireNonNullElse(category, "none");

        return categoryRepository.findByCategory(getCategory)
                .orElseThrow(() -> new AccountBookErrorException(AccountBookErrorCode.NOT_FOUND_CATEGORY));
    }

    private List<AccountBook> createAccountBookList(UserEntity user, AccountBookSpendListRequest requestList) {

        return requestList.accountBookSpendRequestList().stream()
                .map(request -> {
                    Category category = getCategory(request.category());

                    return request.toEntity(user, category);
                })
                .toList();
    }

    private List<AccountBookMonthResponse> monthAccountBookList(List<AccountBook> findList, LocalDate startDate, LocalDate endDate) {
        List<AccountBookMonthResponse> responseList = new ArrayList<>();
        Map<LocalDate, List<AccountBook>> accountBookMap = groupAccountBooksByDate(findList);

        for (int i = startDate.getDayOfMonth(); i <= endDate.getDayOfMonth(); i++) {
            LocalDate currentDate = LocalDate.of(startDate.getYear(), startDate.getMonthValue(), i);
            List<AccountBook> dayBook = accountBookMap.getOrDefault(currentDate, Collections.emptyList());

            AccountBookMonthResponse response = createDailyResponse(dayBook, i);
            responseList.add(response);
        }

        return responseList;
    }

    private Map<LocalDate, List<AccountBook>> groupAccountBooksByDate(List<AccountBook> findList) {
        return findList.stream()
                .collect(Collectors.groupingBy(AccountBook::getOccurredAt));
    }

    private AccountBookMonthResponse createDailyResponse(List<AccountBook> dayBook, int dayOfMonth) {
        int spendTotal = calculateTotalByType(dayBook, CategoryType.SPEND);
        int incomeTotal = calculateTotalByType(dayBook, CategoryType.INCOME);
        List<AccountBookPeriodResponse> periodResponseList = createPeriodResponses(dayBook);

        return new AccountBookMonthResponse(dayOfMonth, spendTotal, incomeTotal, periodResponseList);
    }

    private int calculateTotalByType(List<AccountBook> books, CategoryType type) {

        return books.stream()
                .filter(book -> book.getType() == type)
                .mapToInt(AccountBook::getAmount)
                .sum();
    }

    private List<AccountBookPeriodResponse> createPeriodResponses(List<AccountBook> books) {

        return books.stream()
                .map(AccountBookPeriodResponse::from)
                .toList();
    }

    private void handleBudgetLimitAlert(UserEntity user) {
        int month = LocalDate.now().getMonthValue();

        budgetRepository.findByUserIdAndMonth(user.getId(), month)
                .ifPresent(findBudget -> {
                    BudgetResponse response = budgetRepository.findBudgetByUserIdWithTotal(user.getId());
                    double ratio = (double) response.total() / response.budget();
                    BudgetLimit currentLimit = BudgetLimit.limitBudget(ratio);

                    if (currentLimit != null && currentLimit.getLimitCount() > findBudget.getLimitCount()) {
                        long usageRate = calcUsageRate(response);

                        // 업적
                        publisher.publishEvent(new GetWarningBudgetEvent(user));
                        // fcm 알림
                        publisher.publishEvent(new LimitWarningFcmEvent(user.getId(), user.getNickname(),
                                response.budget(), response.total(), usageRate));

                        findBudget.modifyLimitCount(currentLimit.getLimitCount());
                    }
                });
    }


    private long calcUsageRate(BudgetResponse response) {
        return (response.total() / response.budget()) * 100;
    }

    public String sendMessage(UserEntity user) {
        int month = LocalDate.now().getMonthValue();
        Budget budget = budgetRepository.findByUserIdAndMonth(user.getId(), month).orElseThrow(() -> new AccountBookErrorException(AccountBookErrorCode.NOT_FOUND_BUDGET));
        BudgetResponse response = budgetRepository.findBudgetByUserIdWithTotal(user.getId());

        double ratio = (double) response.total() / response.budget();
        BudgetLimit currentLimit = BudgetLimit.limitBudget(ratio);

        if (currentLimit != null && currentLimit.getLimitCount() > budget.getLimitCount()) {
            long usageRate = calcUsageRate(response);

            return user.getName() + "님, 현재까지 지출은 " + response.total() + "원입니다." +
                    "정하신 예산" + response.budget() + "원 에서" + usageRate + "% 만큼 사용하셨습니다.";
        }

        return "발송된 메시지가 없습니다.";
    }

    @Transactional(readOnly = true)
    public List<TempAccountBookResponse> getTempList(Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));

        return tempAccountBookRepository.findAllByUserId(user.getId()).stream()
                .map(TempAccountBookResponse::from)
                .toList();
    }
}
