package SplitWise.Split;

import SplitWise.Models.Transaction;

import java.util.List;

public interface SplitStrategy {

    SplitStrategyType getStrategyType();
    List<Transaction> split(Expense expense);
}
