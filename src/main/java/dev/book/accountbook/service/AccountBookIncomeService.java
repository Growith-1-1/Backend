package dev.book.accountbook.service;

import dev.book.accountbook.dto.request.AccountBookIncomeRequest;
import dev.book.accountbook.dto.request.AccountBookRequest;
import dev.book.accountbook.dto.response.AccountBookListResponse;
import dev.book.accountbook.dto.response.AccountBookResponse;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.exception.accountbook.AccountBookErrorCode;
import dev.book.accountbook.exception.accountbook.AccountBookErrorException;
import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.accountbook.type.CategoryType;
import dev.book.achievement.achievement_user.dto.event.CreateFirstIncomeEvent;
import dev.book.global.entity.Category;
import dev.book.global.repository.CategoryRepository;
import dev.book.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AccountBookIncomeService {
    private final CategoryRepository categoryRepository;
    private final AccountBookRepository accountBookRepository;

    private final int PAGE_SIZE = 10;

    private final ApplicationEventPublisher publisher;

    @Transactional(readOnly = true)
    public AccountBookResponse getIncomeOne(Long id, Long userId) {
        AccountBook accountBook = findAccountBookOrThrow(id, AccountBookErrorCode.NOT_FOUND_INCOME);

        return AccountBookResponse.from(accountBook);
    }

    @Transactional(readOnly = true)
    public AccountBookListResponse getIncomeList(Long userId, int page) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
        Page<AccountBook> accountBooks = accountBookRepository.findByAccountBookWithPage(userId, CategoryType.INCOME, pageable);

        List<AccountBookResponse> accountBookResponseList = accountBooks.getContent().stream()
                .map(AccountBookResponse::from)
                .toList();

        return new AccountBookListResponse(
                accountBookResponseList,
                accountBooks.getTotalPages(),
                accountBooks.getTotalElements(),
                accountBooks.getNumber() + 1
        );
    }

    @Transactional
    public AccountBookResponse createIncome(AccountBookIncomeRequest request, UserEntity user) {
        Category category = getCategory(request.category());
        AccountBook accountBook = request.toEntity(user, category);
        AccountBook saved = accountBookRepository.save(accountBook);

        if (request.repeat() != null)
            publisher.publishEvent(new CreateFirstIncomeEvent(user));

        return AccountBookResponse.from(saved);
    }

    @Transactional
    public AccountBookResponse modifyIncome(Long id, AccountBookIncomeRequest request, Long userId) {
        AccountBook accountBook = findAccountBookOrThrow(id, AccountBookErrorCode.NOT_FOUND_INCOME);
        updateAccountBook(accountBook, request);
        accountBookRepository.flush();

        return AccountBookResponse.from(accountBook);
    }

    @Transactional
    public boolean deleteIncome(Long id, Long userId) {
        AccountBook accountBook = findAccountBookOrThrow(id, AccountBookErrorCode.NOT_FOUND_INCOME);
        accountBookRepository.delete(accountBook);

        return true;
    }

    private Category getCategory(String category) {
        String getCategory = Objects.requireNonNullElse(category, "none");

        return categoryRepository.findByCategory(getCategory)
                .orElseThrow(() -> new AccountBookErrorException(AccountBookErrorCode.NOT_FOUND_CATEGORY));
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

    private AccountBook findAccountBookOrThrow(Long id, AccountBookErrorCode errorCode) {

        return accountBookRepository.findById(id)
                .orElseThrow(() -> new AccountBookErrorException(errorCode));
    }
}
