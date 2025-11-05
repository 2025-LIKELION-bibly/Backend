package likelion.bibly.global.util;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class UuidGenerator {

	public String generate() {
		return UUID.randomUUID().toString();
	}
}
