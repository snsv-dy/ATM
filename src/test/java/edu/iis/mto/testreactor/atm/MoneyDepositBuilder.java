package edu.iis.mto.testreactor.atm;

import edu.iis.mto.testreactor.atm.bank.Bank;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class MoneyDepositBuilder {
    List<BanknotesPack> banknotes = new ArrayList<>();

    public MoneyDepositBuilder addNotes(Banknote banknote, int count){
        if(banknote != null && count > 0)
            banknotes.add(BanknotesPack.create(count, banknote));

        return this;
    }

    public MoneyDeposit build(){
        return MoneyDeposit.create(Currency.getInstance("PLN"), banknotes);
    }

    public static MoneyDeposit standard_deposit = (new MoneyDepositBuilder())
            .addNotes(Banknote.PL_10, 10)
            .addNotes(Banknote.PL_20, 10)
            .addNotes(Banknote.PL_50, 10)
            .addNotes(Banknote.PL_100, 10)
            .addNotes(Banknote.PL_200, 10)
            .build();
    public static MoneyDeposit empty_deposit = (new MoneyDepositBuilder()).build();
}
