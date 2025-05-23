package SplitWise.Models;

import java.util.List;

public record Group (String id, String name, String description, List<String> members) {
}
