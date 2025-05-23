package SplitWise.Split;

import SplitWise.Models.Split;
import SplitWise.Models.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class Expense{
    @Getter
    private final String expenseId;
    @Getter
    private final String paidBy;
    @Getter
    private final List<String> owers;
    @Getter
    private final Double totalAmount;
    @Getter
    private final SplitStrategy splitStrategy;

    private Expense(ExpenseBuilder builder){
        this.expenseId = builder.expenseId;
        this.paidBy = builder.paidBy;
        this.owers = builder.owers;
        this.totalAmount = builder.totalAmount;
        this.splitStrategy = builder.splitStrategy;
    }

    public List<Transaction> getTransactions(){
        return splitStrategy.split(this);
    }

    public SplitStrategyType getSplitStrategyType() {
        return splitStrategy.getStrategyType();
    }

    public static class ExpenseBuilder{
        private String expenseId;
        private String paidBy;
        private List<String> owers;
        private Double totalAmount;
        private SplitStrategy splitStrategy;
        public ExpenseBuilder setExpenseId(String expenseId){
            this.expenseId = expenseId;
            return this;
        }
        public ExpenseBuilder setPaidBy(String paidBy){
            this.paidBy = paidBy;
            return this;
        }
        public ExpenseBuilder setOwers(List<String> owers){
            this.owers = owers;
            return this;
        }
        public ExpenseBuilder setTotalAmount(Double totalAmount){
            this.totalAmount = totalAmount;
            return this;
        }
        public ExpenseBuilder setSplitStrategy(SplitStrategy splitStrategy){
            this.splitStrategy = splitStrategy;
            return this;
        }
        public Expense build(){
            return new Expense(this);
        }
    }
}


