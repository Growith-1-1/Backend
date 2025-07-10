# 더함🐖 - 챌린지를 통한 절약 습관 생성 가계부 💸📖

<table border="0" cellpadding="0" cellspacing="0">
  <tr>
   <td>
      <img src="https://github.com/user-attachments/assets/fee1201b-8225-4b26-be88-a0eaf84837ca" alt="더함 로고" width="300"/>
   </td>
    <td>
      <h3>
        “사람들과 함께 참여하는 절약 챌린지를 통해 꾸준히 가계부를 작성해요”
      </h3>
      <p>
       참여 인원 : 5명 / 
       개발 주기 : 약 2주 (18일) / 
       Growith 1기 우승 👑
      </p>
    </td>
    
  </tr>
</table>

## 제안 배경 🧐

<img src="https://github.com/user-attachments/assets/137fe977-3514-4c3c-9acb-2ccb1b02e257" alt="제안 배경" width="800"/>

1000명 중 33.6%만이 가계부를 작성하며 즉, 3명 중 1명만이 소비 기록을 실천

응답자의 60%가 가계부 작성이 소득 관리에 도움이 된다고 답했지만, 귀찮음, 절약 효과에 대한 회의감, 작성 유무에 따른 행동 변화의 부재 등을 이유로 가계부 작성을 꺼려함.

이러한 문제를 해결하고자, **절약 챌린지 기능을 통해 사용자가 목표를 설정하고, 다른 사람들과 함께 경쟁하며 동기부여를 받을 수 있도록 기획.**

**혼자 작성하는 것이 아닌, 함께하는 재미와 성취감을 제공함으로써 꾸준한 사용 유도** 

## Tech 🔧

<table border="0" cellpadding="5" cellspacing="0">
  <tr>
    <th>카테고리</th>
    <th>기술 스택</th>
    <th>상세 내용</th>
  </tr>
  <tr>
    <td><strong>백엔드</strong></td>
    <td>Spring Boot 3.4 / Spring Security, Java 17</td>
    <td>
      라이브러리: Spring Data JPA, QueryDSL, JUnit5, Mockito<br/>
      프로토콜: WebSocket, SSE<br/>
      인프라: Firebase, AWS EC2/RDS, GitHub Actions
    </td>
  </tr>
  <tr>
    <td><strong>프론트엔드</strong></td>
    <td>Next.js, React, PWA</td>
    <td>
      주요 라이브러리: TanStack Query, Axios, React-Hook-Form, Zod, shadcn/ui
    </td>
  </tr>
  <tr>
    <td><strong>협업</strong></td>
    <td>Notion, Swagger</td>
    <td>협업 툴로 프로젝트 관리 및 API 문서화</td>
  </tr>
</table>

## 아키텍처 👨‍💻

![image](https://github.com/user-attachments/assets/b63372ee-f2e4-49e8-a954-1f696cabd5ac)


## 구현 기능 🖥️

### 주요 화면 
![image](https://github.com/user-attachments/assets/b1df04dc-873e-4b98-a651-be329c0bd17d)

### 1. 로그인 및 메인 페이지
 - 카카오 OAuth2 로그인을 이용해 로그인/회원가입을 할 수 있어요.
 - 회원가입 시 사용할 닉네임과 주로 사용할 카테고리를 선정할 수 있어요.
 - 메인 페이지에서는 이번 달 소비 현황과 거래 내역, 챌린지에 대한 요약을 볼 수 있어요.

  ![image](https://github.com/user-attachments/assets/fa72ad40-266f-491b-889f-ea2f474d4489)

### 2. 수입 / 지출 등록 및 내역 확인
 - 단건 또는 일정 주기의 수입 / 지출을 등록할 수 있어요.
 - Codef API를 사용하여 계좌를 연결하고 실제 소비 내역을 가져올 수 있어요.
 - 이번 달 예산을 설정하여 과소비 방지 알림을 받을 수 있어요.

![image](https://github.com/user-attachments/assets/bc0bbe84-6515-4ac2-b976-8d1dd3b41c15)

### 3. 통계 확인
 - 어제, 지난주, 저번달과 비교하여 통계를 볼 수 있어요.
 - 카테고리 별로 얼만큼 소비했는지 확인할 수 있어요.

![image](https://github.com/user-attachments/assets/9a56637b-d62d-4cf1-8f9f-9e6c99c893f0)

### 4. 챌린지
 - 챌린지를 등록하고 다른사람과 함께 도전할 수 있어요.
 - 성공, 실패한 챌린지를 확인할 수 있어요.
 - 챌린지 상세를 확인할 수 있어요.

![image](https://github.com/user-attachments/assets/94e0a6ef-31ee-4cac-894a-a523127426b4)

### 5. 카카오톡 친구 초대
 - 카카오톡으로 초대 링크를 보내고 '더함'에 친구를 초대할 수 있어요.
 - 친구는 내가 만든 챌린지에 초대하여 함께 참여할 수 있어요.

<img src="https://github.com/user-attachments/assets/77a268d9-b754-4208-aba9-397ceadb0051" alt="친구 초대" width="450"/>

### 6. 마이페이지
 - 나의 프로필을 관리할 수 있어요.
 - '더함'을 사용하면서 달성한 업적들을 확인할 수 있어요.
 - '더함'을 사용하면서 진행한 챌린지에 대한 통계를 확인할 수 있어요.
 - 회원가입 시 설정했던 주 카테고리를 변경할 수 있어요.
 - Codef API에 연동할 계좌 내역을 작성할 수 있어요.

![image](https://github.com/user-attachments/assets/c53e7d7b-abd8-4776-9536-0a197170cef1)

## 문제 해결 💡

1. 트랜잭션과 이벤트 기반 설계
 > * 모듈 간의 결합도를 줄이기 위해 이벤트 기반의 설계 진행
>  * 트랜잭션이 정상적으로 이어지지 않는 문제 발생
> * 이벤트 리스너의 트랜잭션에 REQUIRED_NEW 전파 타입을 적용하여 독립 트랜잭션 처리
> * 데이터 일관성과 안전성 확보

2. N+1 문제 해결
  > * JPA Lazy 설정에 따른 N+1 문제 발생
  > * Fetch Join과 Entity Graph를 사용하여 연관 객체를 효율적으로 로딩

3. 친구 초대 유저 추적
  > * '카카오톡 공유하기' 기능을 통해 유입된 유저가 어떤 유저를 통해 초대 되었는지 파악 필요
  > * 인증되지 않은 유입에 관하여 초대 기능 구현에 어려움을 겪음
  > * 초대한 사람을 식별하기 위해 유저별 고유 토큰을 링크에 삽입
  > * 유입자는 쿠키로 해당 토큰을 들고 다니며 초대 관계 유지
  > * 로그인 시에 커스텀 Security Filter를 구성하여 초대 정보 매

4. 락 기능을 통한 동시성 문제 해결
  > * 참여인원이 제한된 챌린지에서는 여러 사용자의 동시 접근으로 동시성 문제 발생 가능성
  > * 초기에는 비관적 락(Pessimistic Lock)을 활용하여 데이터 정합성을 보장
  > * 이후 충돌 가능성이 낮은 상태에서 락 점유 구조가 비효율적이라고 판단하여 낙관적 락(Optimistic Lock) 적용

## 성능 최적화 및 개선 ♒

1. 22만 건 조회 쿼리 약 7.5배 이상의 성능 개선
  > * 소비 내역 조회 시, 연 단위 데이터가 약 22만 건까지 증가할 수 있는 상황을 가정
  > * 인덱싱 및 커버링 인덱스를 도입하여 420ms 에서 41ms, 약 7.5배 이상의 성능 개선을 달성.
>
> 관련 포스팅 : [커버링 인덱스를 이용한 최적화](https://saltfn.tistory.com/entry/%EC%BB%A4%EB%B2%84%EB%A7%81-%EC%9D%B8%EB%8D%B1%EC%8A%A4%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EC%B5%9C%EC%A0%81%ED%99%94)
    
2. AES 알고리즘으로 은행 데이터 암호화화
  > * Code API를 통해 소비 내역 수집 시, 사용자 계좌번호·은행 ID·비밀번호 등의 민감 정보가 필요
  > * 보안 강화를 위해 같은 평문이라도 다른 암호문이 반환되도록 AES-CBC(Cipher Block Chaining) + PKCS5Padding 방식으로 사용자 계좌 정보 암호화
  > * AES에 사용된 키는 환경변수로 처리하여 외부 노출에 방지
  > * 정보 노출에 대한 위협을 최소화 및 데이터 보호에 집중.

## 개발 일정 📆

<img src="https://github.com/user-attachments/assets/aa4c2518-00b3-43d4-a915-019cf46cff8f" alt="개발 일정" width="800"/>

## 팀 소개 👥

--- 
### PM
|<a href="https://github.com/X1n9fU">김민경</a>|
|---|
|<img src="https://github.com/X1n9fU.png" width="100px;" alt="김민경 프로필"/>|

### Frontend

|<a href="https://github.com/yeonna18k">강나연</a>|<a href="https://github.com/tlsgptj">신혜서</a>|
|------|---|
|<img src="https://github.com/yeonna18k.png" width="100px;" alt="강나연 프로필"/>|<img src="https://github.com/tlsgptj.png" width="100px;" alt="신혜서 프로필"/>|

### Backend

|<a href="https://github.com/BHC-Chicken">박현철</a>|<a href="https://github.com/2unmini">천준민</a>|<a href="https://github.com/X1n9fU">김민경</a>|
|------|---|---|
|<img src="https://github.com/BHC-Chicken.png" width="100px;" alt="박현철 프로필"/>|<img src="https://github.com/2unmini.png" width="100px;" alt="천준민 프로필"/>|<img src="https://github.com/X1n9fU.png" width="100px;" alt="김민경 프로필"/>|
