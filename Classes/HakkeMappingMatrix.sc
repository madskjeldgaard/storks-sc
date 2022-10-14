/*
* TODO
* Function Chain
*/

/*
HakkeMappingMatrix{
	classvar <instances;
	var proxy, <window;
	var hakkebraet, layer, page, <matrix, buttonMatrixState;

	var encoderCallbacks;

	*initClass{
		Class.initClassTree(IdentityDictionary);
		// This is necessary to make it possible to query global instances before the first instance is created
		instances = IdentityDictionary.new();
	}

	*new { |nodeproxy, hakkebraetInstance, layerNum=0, pageNum=0|
		^super.new.init(nodeproxy, hakkebraetInstance, layerNum, pageNum);
	}

	init{|nodeproxy, hakkebraetInstance, layerNum, pageNum|
		layer = layerNum;
		page = pageNum;
		proxy = nodeproxy;
		hakkebraet = hakkebraetInstance;
		encoderCallbacks = Array.newClear(hakkebraet.numEncoders);
		this.openWindow();
		this.addInstanceToGlobals();
	}

	addInstanceToGlobals{
		var lookup = proxy.key.asSymbol;
		if(instances.at(lookup).isNil, {
			instances.put(lookup, this)
		})
	}

	openWindow{
		var header = StaticText.new().string_(
			"HakkeMatrix ch % page % mapped to NodeProxy %".format(layer, page, proxy.key)
		)
		.font_(
			Font.sansSerif.size_(16).italic_(true)
		);

		// Create window
		window = Window.new(name: "HakkeMatrix %".format(proxy.key));

		// Create and attach functions to be performed on button press
		this.registerMatrixButtonActions();

		// Button layout
		window.layout_(
			VLayout(
				header,
				matrix.layout
			)
		);

		// If window was open before, recall state of buttons
		if(buttonMatrixState.isNil.not, {
			this.recallState()
		});

		// Make window visible
		window.front();
	}

	saveState{
		buttonMatrixState = matrix.rows.collect{|row|
			row.collect{|button|
				button.value;
			}
		}
	}

	recallState{
		buttonMatrixState.do{|row, rowNum|
			row.do{|rowButtonState, rowButton|
				// "Row %, button %, state: %".format(rowNum, rowButton, rowButtonState).postln;
				matrix.rows[rowNum][rowButton].value_(rowButtonState)
			}
		}
	}

	registerMatrixButtonActions{
		var rowLabels = proxy.controlKeys;
		var buttonStates = [
			[" "],
			["normal"],
			["reverse"],
			["sine"],
			["random"],
		];

		matrix = GuiButtonMatrix.new(
			rowLabels.size,
			hakkebraet.numEncoders,
			rowLabels,
			// @TODO: Buttons as well!!!
			hakkebraet.numEncoders.collect{|encNum|
				"enc%".format(encNum)
			},
			buttonStates
		);

		rowLabels.do{|ckey, ckeyIndex|
			hakkebraet.numEncoders.do{|encNum|
				encoderCallbacks[encNum] = IdentityDictionary.new;
				matrix.action_(
					encNum,
					ckeyIndex,
					{|el|
						var spec = this.getSpec(ckey);

						var callback;

						this.saveState();

						callback = switch(el.value,
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

							encoderCallbacks[encNum][ckey] = callback;
							// encoderCallbacks[encNum]
							hakkebraet.register(layer, page, encNum, encoderCallbacks[encNum]);
					}
				);
			}
		};

	}

	getCallback{|controlKey, callbackName|
		var spec = this.getSpec(controlKey);
		var callbacks;

		callbacks = IdentityDictionary[
			\nothing -> {|val, chan|
			},
			\normal -> {|val, chan|
				// @TODO do not hardcore scaling here!
				proxy.set(controlKey, spec.map(val));
			},
			\inverted ->  {|val, chan|
				// @TODO do not hardcore scaling here!
				proxy.set(controlKey, spec.map(1.0-val ));
			},
			\sine -> {|val, chan|
				// @TODO do not hardcore scaling here!
				val = Env.sine[val];
				proxy.set(controlKey, spec.map(val));
			},
			\random -> {|val, chan|
				var envSize = 3;

				// @TODO do not hardcode scaling here!
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
		^ // Ndef local spec ?
		proxy.specs.at(controlKey) ??
		// Global spec
		Spec.specs.at(controlKey) ??
		// Default spec
		[0.0, 1.0].asSpec;
	}
}
*/
