/*
* TODO
* Keep track of instances
* Function Chain 
*/
HakkeMappingMatrix{
	var proxy;
	var hakkebraet, layer, page, matrix;

	*new { |nodeproxy, hakkebraetInstance, layerNum=0, pageNum=0|
		^super.new.init(nodeproxy, hakkebraetInstance, layerNum, pageNum);
	}

	init{|nodeproxy, hakkebraetInstance, layerNum, pageNum|
		layer = layerNum;
		page = pageNum;
		proxy = nodeproxy;
		hakkebraet = hakkebraetInstance;
		this.registerMatrixButtonActions();

		Window.new()
		.layout_(
			matrix.layout
		).front();

	}

	registerMatrixButtonActions{
		var rowLabels = proxy.controlKeys;
		matrix = GuiButtonMatrix.new(
			rowLabels.size, 
			hakkebraet.numEncoders, 
			rowLabels, 
			// @TODO: Buttons as well!!!
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
								this.getCallback(ckey, \nothing)
							}, 
							// Normal mapping
							1, { 
								this.getCallback(ckey, \normal)
							},
							// Inverted
							2, { 
								this.getCallback(ckey, \inverted)
							}, 
							// Sine
							3, {
								this.getCallback(ckey, \sine)
							}, 
							// Random
							4, {
								this.getCallback(ckey, \random)
							});

							hakkebraet.register(layer, page, encNum, callback);
					}
				);
			}
		};

	}

	getCallback{|controlKey, callbackName|
		var spec = this.getSpec(controlKey);
		var callbacks = IdentityDictionary[
			\nothing -> {|val, chan| },
			\normal -> {|val, chan|
				// @TODO do not hardcore scaling here!
				proxy.set(controlKey, spec.map(val / hakkebraet.maxMidiVal14Bit));
			},
			\inverted ->  {|val, chan|
				// @TODO do not hardcore scaling here!
				proxy.set(controlKey, spec.map(1.0-(val / hakkebraet.maxMidiVal14Bit)));

			},
			\sine -> {|val, chan|
				// @TODO do not hardcore scaling here!
				val = (val / hakkebraet.maxMidiVal14Bit);
				val = Env.sine[val];
				proxy.set(controlKey, spec.map(val));
			},
			\random -> {|val, chan|
				var envSize = 3;
				// @TODO do not hardcore scaling here!
				val = (val / hakkebraet.maxMidiVal14Bit);
				val = Env(
					levels: Array.rand(envSize, 0.0000001,1.0), 
					times: Array.rand(envSize - 1,0.000001,1.0).normalizeSum,
					curve: Array.rand(envSize - 1, -10.0,10.0)
				)[val];
				proxy.set(controlKey, spec.map(val));
			}
		];

		^callbacks[callbackName]
	}

	getSpec{|controlKey|
		^Spec.specs.at(controlKey) ?? 
		// Ndef local spec ? 
		this.specs.at(controlKey) ??
		// Default spec
		[0.0, 1.0].asSpec;
	}
}
