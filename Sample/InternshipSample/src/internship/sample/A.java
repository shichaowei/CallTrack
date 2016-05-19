package internship.sample;

public class A {
	public void m1() {
		
		C cObject = new C();
		this.m2();
		
		cObject.m2();
		
	}

	public void m2() {
		B bObject= new B();
		int x = bObject.m1();
	}

	public void m3() {

	}
}
