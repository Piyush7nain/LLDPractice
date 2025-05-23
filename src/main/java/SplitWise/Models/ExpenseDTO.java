package SplitWise.Models;

import SplitWise.Split.SplitStrategyType;

import java.util.List;

public record ExpenseDTO(String id, String creatorId, List<String> owersId, String totalAmount, SplitStrategyType splitStrategyType) {
}
