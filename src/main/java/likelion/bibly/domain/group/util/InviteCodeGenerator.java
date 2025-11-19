package likelion.bibly.domain.group.util;

import java.util.Random;

public class InviteCodeGenerator {
	private static final Random RANDOM = new Random();
	private static final int CODE_LENGTH = 4;

	public static String generate() {
		StringBuilder code = new StringBuilder();
		for (int i = 0; i < CODE_LENGTH; i++) {
			code.append(RANDOM.nextInt(10));
		}
		return code.toString();
	}
}
