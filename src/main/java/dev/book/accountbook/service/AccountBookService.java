package dev.book.accountbook.service;

import dev.book.accountbook.dto.event.SpendCreatedEvent;
import dev.book.accountbook.dto.request.AccountBookIncomeRequest;
import dev.book.accountbook.dto.request.AccountBookRequest;
import dev.book.accountbook.dto.request.AccountBookSpendListRequest;
import dev.book.accountbook.dto.request.AccountBookSpendRequest;
import dev.book.accountbook.dto.response.AccountBookIncomeResponse;
import dev.book.accountbook.dto.response.AccountBookSpendResponse;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.exception.accountbook.AccountBookErrorCode;
import dev.book.accountbook.exception.accountbook.AccountBookErrorException;
import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.accountbook.repository.BudgetRepository;
import dev.book.accountbook.type.CategoryType;
import dev.book.achievement.achievement_user.IndividualAchievementStatusService;
import dev.book.challenge.rank.SpendCreatedRankingEvent;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AccountBookService {
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final ApplicationEventPublisher publisher;
    private final CategoryRepository categoryRepository;
    private final AccountBookRepository accountBookRepository;

    private final IndividualAchievementStatusService individualAchievementStatusService;

    @Transactional
    public AccountBookSpendResponse getSpendOne(Long id, Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));
        AccountBook accountBook = findAccountBookOrThrow(id, userId, AccountBookErrorCode.NOT_FOUND_SPEND);

        return AccountBookSpendResponse.from(accountBook);
    }

    @Transactional
    public List<AccountBookSpendResponse> getSpendList(Long userId) {
        List<AccountBook> accountBooks = accountBookRepository.findAllByUserIdAndTypeAndCategoryIsNotNullOrderByUpdatedAtDesc(userId, CategoryType.SPEND);

        return accountBooks.stream()
                .map(AccountBookSpendResponse::from)
                .toList();
    }

    @Transactional
    public AccountBookSpendResponse createSpend(AccountBookSpendRequest request, UserEntity user) {
        Category category = getCategory(request.category());
        AccountBook accountBook = request.toEntity(user, category);
        AccountBook saved = accountBookRepository.save(accountBook);

        if (budgetRepository.existsById(user.getId())) {
            publisher.publishEvent(new SpendCreatedEvent(user.getId(), user.getNickname()));
        }

        publisher.publishEvent(new SpendCreatedRankingEvent(accountBook));

        return AccountBookSpendResponse.from(saved);
    }

    @Transactional
    public AccountBookSpendResponse modifySpend(AccountBookSpendRequest request, Long id, Long userId) {
        AccountBook accountBook = findAccountBookOrThrow(id, userId, AccountBookErrorCode.NOT_FOUND_SPEND);
        updateAccountBook(accountBook, request);
        accountBookRepository.flush();

        return AccountBookSpendResponse.from(accountBook);
    }

    @Transactional
    public boolean deleteSpend(Long id, Long userId) {
        AccountBook accountBook = findAccountBookOrThrow(id, userId, AccountBookErrorCode.NOT_FOUND_SPEND);
        accountBookRepository.delete(accountBook);

        return true;
    }

    @Transactional
    public AccountBookIncomeResponse getIncomeOne(Long id, Long userId) {
        AccountBook accountBook = findAccountBookOrThrow(id, userId, AccountBookErrorCode.NOT_FOUND_INCOME);

        return AccountBookIncomeResponse.from(accountBook);
    }

    @Transactional
    public List<AccountBookIncomeResponse> getIncomeList(Long userId) {
        List<AccountBook> accountBooks = accountBookRepository.findAllByUserIdAndTypeAndCategoryIsNotNullOrderByUpdatedAtDesc(userId, CategoryType.INCOME);

        return accountBooks.stream()
                .map(AccountBookIncomeResponse::from)
                .toList();
    }

    @Transactional
    public AccountBookIncomeResponse createIncome(AccountBookIncomeRequest request, UserEntity user) {
        Category category = getCategory(request.category());
        AccountBook accountBook = request.toEntity(user, category);
        AccountBook saved = accountBookRepository.save(accountBook);

        if (request.repeat() != null)
            individualAchievementStatusService.setCreateFirstIncomeTrue(user);

        return AccountBookIncomeResponse.from(saved);
    }

    @Transactional
    public AccountBookIncomeResponse modifyIncome(Long id, AccountBookIncomeRequest request, Long userId) {
        AccountBook accountBook = findAccountBookOrThrow(id, userId, AccountBookErrorCode.NOT_FOUND_INCOME);
        updateAccountBook(accountBook, request);
        accountBookRepository.flush();

        return AccountBookIncomeResponse.from(accountBook);
    }

    @Transactional
    public boolean deleteIncome(Long id, Long userId) {
        AccountBook accountBook = findAccountBookOrThrow(id, userId, AccountBookErrorCode.NOT_FOUND_INCOME);
        accountBookRepository.delete(accountBook);

        return true;
    }

    public List<AccountBookSpendResponse> getCategorySpendList(String category, Long userId) {
        Page<AccountBook> categorySpendList = accountBookRepository.findByUserIdAndCategoryNameWithGraph(userId, category, PageRequest.of(0, 10));

        return categorySpendList.stream()
                .map(AccountBookSpendResponse::from)
                .toList();
    }

    public List<AccountBookSpendResponse> createSpendList(UserEntity user, AccountBookSpendListRequest requestList) {
        List<AccountBook> accountBookList = accountBookList(user, requestList);
        List<AccountBook> savedAccountBookList = accountBookRepository.saveAll(accountBookList);

        return savedAccountBookList.stream()
                .map(AccountBookSpendResponse::from)
                .toList();
    }

    private AccountBook findAccountBookOrThrow(Long id, Long userId, AccountBookErrorCode errorCode) {
        return accountBookRepository.findByIdAndUserId(id, userId)
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

    private List<AccountBook> accountBookList(UserEntity user, AccountBookSpendListRequest requestList) {
        return requestList.accountBookSpendRequestList().stream()
                .map(request -> {
                    Category category = getCategory(request.category());

                    return request.toEntity(user, category);
                }).toList();
    }
}
