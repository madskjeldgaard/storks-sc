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
				matrix.func(
					encNum, 
					ckeyIndex, 
					{|el|
						if(el.value == 1,{
							"Mapping % to % for %".format(encNum, ckey, this.key).postln;

							hakkebraet.register(layer, page, encNum, {|val, chan|

								var spec = 
									// Is there a global spec for this key?
									Spec.specs.at(ckey) ?? 
									// Ndef local spec ? 
									this.specs.at(ckey) ??
									// Default spec
									[0.0, 1.0].asSpec;

									// @TODO do not hardcore scaling here!
								this.set(ckey, spec.map(val / hakkebraet.maxMidiVal14Bit).postln);
							});

						})
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
