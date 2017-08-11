package test.no.javabook.dijoe.testclasses;

import no.javabook.dijoe.annotations.Inject;
import no.javabook.dijoe.annotations.Username;

public class ThingWithUsername {
	final String username;
	
	@Inject
	public ThingWithUsername(@Username String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}

	@Override
	public String toString() {
		return String.format("%s<username=%s>", getClass().getCanonicalName(), username);
	}
}
