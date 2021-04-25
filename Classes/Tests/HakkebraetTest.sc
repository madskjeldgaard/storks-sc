HakkebraetTest1 : UnitTest {
	test_check_classname {
		var result = Hakkebraet.new;
		this.assert(result.class == Hakkebraet);
	}
}


HakkebraetTester {
	*new {
		^super.new.init();
	}

	init {
		HakkebraetTest1.run;
	}
}
