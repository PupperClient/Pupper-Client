package com.soarclient.management.user;

public class User {

	private final String name, uuid;
	private int role, expiration;
	
	public User(String name, String uuid, int role, int expiration) {
		this.name = name;
		this.uuid = uuid;
		this.role = role;
		this.expiration = expiration;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public int getExpiration() {
		return expiration;
	}

	public void setExpiration(int expiration) {
		this.expiration = expiration;
	}

	public String getName() {
		return name;
	}

	public String getUuid() {
		return uuid;
	}
}
