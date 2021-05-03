+NodeProxy{
	hakkeMatrix{|hakkebraet, layer=0, page=0|
		var rowLabels = this.controlKeys;

		var matrix = GuiButtonMatrix.new(
			rowLabels.size, 
			hakkebraet.numEncoders, 
			rowLabels, 
			hakkebraet.numEncoders.collect{|encNum| 
				"enc%".format(encNum) 
			}
		);

		rowLabels.do{|ckey, ckeyIndex|
			hakkebraet.numEncoders.do{|encNum|
				matrix.action_(
					encNum, 
					ckeyIndex, 
					{|el|
						var spec = 
						// Is there a global spec for this key?
						Spec.specs.at(ckey) ?? 
						// Ndef local spec ? 
						this.specs.at(ckey) ??
						// Default spec
						[0.0, 1.0].asSpec;

						var callback = switch(el.value, 
							// Nothing
							0, {  
								{|val, chan| }
							}, 
							// Normal mapping
							1, { 
								{|val, chan|
									// @TODO do not hardcore scaling here!
									this.set(ckey, spec.map(val / hakkebraet.maxMidiVal14Bit));
								}
							},
							// Inverted
							2, { 
								{|val, chan|
									// @TODO do not hardcore scaling here!
									this.set(ckey, spec.map(1.0-(val / hakkebraet.maxMidiVal14Bit)));
								}
							}, 
							// Sine
							3, {
								{|val, chan|
									// @TODO do not hardcore scaling here!
									val = (val / hakkebraet.maxMidiVal14Bit);
									val = Env.sine[val];
									this.set(ckey, spec.map(val));
								}
							}, 
							// Random
							4, {
								{|val, chan|
									var envSize = 3;
									// @TODO do not hardcore scaling here!
									val = (val / hakkebraet.maxMidiVal14Bit);
									val = Env(
										levels: Array.rand(envSize, 0.0000001,1.0), 
										times: Array.rand(envSize - 1,0.000001,1.0).normalizeSum,
										curve: Array.rand(envSize - 1, -10.0,10.0)
									)[val];
									this.set(ckey, spec.map(val));
								}
							});

							hakkebraet.register(layer, page, encNum, callback);
					}
				);
			}
		};
	
		Window.new()
		.layout_(
			matrix.layout
		).front();

	}
}
