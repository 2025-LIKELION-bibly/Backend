package likelion.bibly.global.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

	@GetMapping("/")
	public String root() {
		return "Bibly API Server";
	}
}
