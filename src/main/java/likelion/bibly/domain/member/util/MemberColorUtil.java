package likelion.bibly.domain.member.util;

import java.util.ArrayList;
import java.util.List;

public class MemberColorUtil {
	private static final List<String> ALL_COLORS = List.of(
		"RED", "BLUE", "GREEN", "YELLOW",
		"PURPLE", "ORANGE", "PINK", "CYAN"
	);

	public static List<String> getAvailableColors(List<String> usedColors) {
		List<String> availableColors = new ArrayList<>(ALL_COLORS);
		availableColors.removeAll(usedColors);
		return availableColors;
	}

	public static boolean isValidColor(String color) {
		return ALL_COLORS.contains(color);
	}

	public static List<String> getAllColors() {
		return ALL_COLORS;
	}
}
