package dev.book.accountbook.service;

import dev.book.accountbook.dto.event.SpendCreatedEvent;
import dev.book.accountbook.dto.request.AccountBookIncomeRequest;
import dev.book.accountbook.dto.request.AccountBookRequest;
import dev.book.accountbook.dto.request.AccountBookSpendRequest;
import dev.book.accountbook.dto.response.AccountBookIncomeResponse;
import dev.book.accountbook.dto.response.AccountBookSpendResponse;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.exception.accountbook.AccountBookErrorCode;
import dev.book.accountbook.exception.accountbook.AccountBookErrorException;
import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.accountbook.type.Category;
import dev.book.accountbook.type.CategoryType;
import dev.book.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountBookService {
    private final AccountBookRepository accountBookRepository;
    private final ApplicationEventPublisher publisher;

    public AccountBookSpendResponse getSpendOne(Long id, Long userId) {
        AccountBook accountBook = findAccountBookOrThrow(id, userId, AccountBookErrorCode.NOT_FOUND_SPEND);

        return AccountBookSpendResponse.from(accountBook);
    }

    public List<AccountBookSpendResponse> getSpendList(Long userId) {
        List<AccountBook> accountBooks = accountBookRepository.findAllByUserIdAndTypeOrderByUpdatedAtDesc(userId, CategoryType.SPEND);

        return accountBooks.stream()
                .map(AccountBookSpendResponse::from)
                .toList();
    }

    public AccountBookSpendResponse createSpend(AccountBookSpendRequest spendRequest, UserEntity user) {
        AccountBook accountBook = accountBookRepository.save(spendRequest.toEntity(user));
        publisher.publishEvent(new SpendCreatedEvent(user.getId(), user.getNickname()));

        return AccountBookSpendResponse.from(accountBook);
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
        accountBookRepository.deleteById(accountBook.getId());

        return true;
    }

    public AccountBookIncomeResponse getIncomeOne(Long id, Long userId) {
        AccountBook accountBook = findAccountBookOrThrow(id, userId, AccountBookErrorCode.NOT_FOUND_INCOME);

        return AccountBookIncomeResponse.from(accountBook);
    }

    public List<AccountBookIncomeResponse> getIncomeList(Long userId) {
        List<AccountBook> accountBooks = accountBookRepository.findAllByUserIdAndTypeOrderByUpdatedAtDesc(userId, CategoryType.INCOME);

        return accountBooks.stream()
                .map(AccountBookIncomeResponse::from)
                .toList();
    }

    public AccountBookIncomeResponse createIncome(AccountBookIncomeRequest request, UserEntity user) {
        AccountBook accountBook = accountBookRepository.save(request.toEntity(user));

        return AccountBookIncomeResponse.from(accountBook);
    }

    @Transactional
    public AccountBookIncomeResponse modifyIncome(Long id, AccountBookIncomeRequest request, Long userId) {
        AccountBook accountBook = findAccountBookOrThrow(id, userId, AccountBookErrorCode.NOT_FOUND_INCOME);
        updateAccountBook(accountBook, request);

        return AccountBookIncomeResponse.from(accountBook);
    }

    @Transactional
    public boolean deleteIncome(Long id, Long userId) {
        AccountBook accountBook = findAccountBookOrThrow(id, userId, AccountBookErrorCode.NOT_FOUND_INCOME);
        accountBookRepository.deleteById(accountBook.getId());

        return true;
    }

    public List<AccountBookSpendResponse> getCategorySpendList(Category category, Long userId) {
        List<AccountBook> categorySpendList = accountBookRepository.findByUserIdAndCategoryOrderByUpdatedAtDescIdDesc(userId, category);

        return categorySpendList.stream()
                .map(AccountBookSpendResponse::from)
                .toList();
    }

    private AccountBook findAccountBookOrThrow(Long id, Long userId, AccountBookErrorCode errorCode) {
        return accountBookRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AccountBookErrorException(errorCode));
    }

    private void updateAccountBook(AccountBook accountBook, AccountBookRequest request) {
        accountBook.modifyTitle(request.title());
        accountBook.modifyCategory(request.category());
        accountBook.modifyAmount(request.amount());
        accountBook.modifyMemo(request.memo());
        accountBook.modifyFrequency(request.repeat().frequency());
        accountBook.modifyMonth(request.repeat().month());
        accountBook.modifyDay(request.repeat().day());
        accountBook.modifyEndDate(request.endDate());
    }
}
