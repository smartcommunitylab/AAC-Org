package it.smartcommunitylab.orgmanager.componentsmodel;

/**
 * Class that contains the data structure for the user that is going to be handled
 *
 */
public class UserInfo {
	private String username;
	private String name;
	private String surname;
	/**
	 * Constructor for creating new user
	 * @param username
	 * @param name
	 * @param surname
	 */
	public UserInfo(String username, String name, String surname) {
		this.username = username;
		this.name = name;
		this.surname = surname;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getSurname() {
		return surname;
	}
	
	public void setSurname(String surname) {
		this.surname = surname;
	}
	
	@Override
	public String toString() {
		return "Username: " + username + ", Name: " + name + ", Surname: " + surname;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof UserInfo) {
			UserInfo user = (UserInfo) o;
			if (user.username.equals(username) && user.name.equals(name) && user.surname.equals(surname))
				return true;
		}
		return false;
	}
}
