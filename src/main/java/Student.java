
public class Student {
	private String id;
	private String major;
	private char gender;

	public Student(String id, String major, char gender) {
		this.id = id;
		this.major = major;
		this.gender = gender;
	}

	public Student() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMajor() {
		return major;
	}

	public void setMajor(String major) {
		this.major = major;
	}

	public char getGender() {
		return gender;
	}

	public void setGender(char gender) {
		this.gender = gender;
	}

	@Override
	public String toString() {
		return "Student [id=" + id + ", major=" + major + ", gender=" + gender + "]";
	}

}
