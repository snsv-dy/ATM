package edu.iis.mto.testreactor.atm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;

import edu.iis.mto.testreactor.atm.bank.AccountException;
import edu.iis.mto.testreactor.atm.bank.AuthorizationException;
import edu.iis.mto.testreactor.atm.bank.AuthorizationToken;
import edu.iis.mto.testreactor.atm.bank.Bank;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ATMachineTest {
    @Mock
    Bank bank;

    private final String card_number = "1234";
    private final Card card = Card.create(card_number);
    private final String pin_number = "1234";
    private final PinCode pin = PinCode.createPIN(1, 2, 3, 4);

    ATMachine atm;

    @BeforeEach
    void setUp() throws Exception {
        atm = new ATMachine(bank, Currency.getInstance("PLN"));
        atm.setDeposit(MoneyDepositBuilder.standard_deposit);

        Mockito.when(bank.autorize(card_number, pin_number))
                .thenReturn(AuthorizationToken.create("any string"));
    }

    @Test
    void setDepositIsWorking(){
        Mockito.reset(bank);
        assertEquals(atm.getCurrentDeposit(), MoneyDepositBuilder.standard_deposit);
    }

    @Test
    void Withdraw200PLN() throws AuthorizationException, ATMOperationException {
        Money required_money = new Money(200);

        Withdrawal expected_withdrawal = Withdrawal.create(List.of(BanknotesPack.create(1, Banknote.PL_200)));
        Withdrawal result = atm.withdraw(pin, card, required_money);

        assertEquals(expected_withdrawal, result);
    }

    @Test
    void Withdraw0PLN() throws AuthorizationException, ATMOperationException {
        Money required_money = new Money(0);

        List<Banknote> emptyList = new ArrayList<>();
        Withdrawal result = atm.withdraw(pin, card, required_money);
        assertEquals(emptyList, result.getBanknotes());
    }

    @Test
    void WithdrawWithMultipleBanknotes() throws AuthorizationException, ATMOperationException {
        Money required_money = new Money(180);

        Withdrawal expected_withdrawal = Withdrawal.create(List.of(
                BanknotesPack.create(1, Banknote.PL_100),
                BanknotesPack.create(1, Banknote.PL_50),
                BanknotesPack.create(1, Banknote.PL_20),
                BanknotesPack.create(1, Banknote.PL_10)
        ));
        Withdrawal result = atm.withdraw(pin, card, required_money);

        assertEquals(expected_withdrawal, result);
    }

    @Test
    void NotEnoughMoneyToWithdraw() throws AuthorizationException {
        atm.setDeposit(MoneyDepositBuilder.empty_deposit);
        Money required_money = new Money(200);

        Throwable thrown_exception = assertThrows(ATMOperationException.class, () -> {
            atm.withdraw(pin, card, required_money);
        });

        assertEquals(ErrorCode.WRONG_AMOUNT, ((ATMOperationException)thrown_exception).getErrorCode());
    }


    @Test
    void ImpossibleAmountOfMoneyToWithdraw() throws AuthorizationException, ATMOperationException  {
        Money required_money = new Money(201);

        Throwable thrown_exception = assertThrows(ATMOperationException.class, () -> {
            atm.withdraw(pin, card, required_money);
        });

        assertEquals(ErrorCode.WRONG_AMOUNT, ((ATMOperationException)thrown_exception).getErrorCode());
    }

    @Test
    void WithdrawNotSupportedCurrency() {
        Money required_money = new Money(100, Currency.getInstance("USD"));
        Mockito.reset(bank);

        Throwable thrown_exception = assertThrows(ATMOperationException.class, () -> {
            atm.withdraw(pin, card, required_money);
        });

        assertEquals(ErrorCode.WRONG_CURRENCY, ((ATMOperationException)thrown_exception).getErrorCode());
    }


    @Test
    void VerifyAuthorizationAndChargingInBank() throws ATMOperationException, AuthorizationException, AccountException {
        Money required_money = new Money(200);

        Withdrawal expected_withdrawal = Withdrawal.create(List.of(BanknotesPack.create(1, Banknote.PL_200)));
        Withdrawal result = atm.withdraw(pin, card, required_money);

        assertEquals(expected_withdrawal, result);

        Mockito.verify(bank).autorize(pin.getPIN(), card.getNumber());
        Mockito.verify(bank).charge(Mockito.any(), eq(required_money));
    }
}
