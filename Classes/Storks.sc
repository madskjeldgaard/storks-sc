/*

@TODO: 7 bit midi functionality

*/
Storks {
	var <responders;

	const <numChannels = 16;
	const <numLayersPerChannel = 4;
	const <numEncoders = 16;
	const <numButtons = 8;

	var <maxMidiVal14Bit = 16363;
	var <maxMidiVal7Bit = 127;

	var scale;
    var <initialized;

	*new { |fourteenBit=true, connectOnInit=true, registerDefaults=true, scaledValues=true|
		^super.new.init(fourteenBit, connectOnInit, registerDefaults, scaledValues);
	}

	init { | fourteenBit, connectOnInit, registerDefaults, scaledValues|
		responders = numChannels.collect{|chanNum|
			numLayersPerChannel.collect{|layerNum|
				Array.newClear(numEncoders)
			}
		};

		scale = scaledValues;

		if(connectOnInit, {
			this.connect();
		});

		if(registerDefaults, {
			numChannels.do{|chanNum|
                numLayersPerChannel.do{|layerNum|
                    numEncoders.do{|encNum|
                        this.registerEnc(chanNum, layerNum, encNum)
                    };
                };
                numButtons.do{|butNum|
                    this.registerButton(chanNum, butNum)
                };

			};
		});

        initialized = true;

		^this
	}

    registerButton{|channel, button, callbackFunctionOn, callbackFunctionOff|
        var label = "storks_chan%_button%".format(channel, button).asSymbol;
        var offset = 44;
        var noteNum = button + offset;
        var chan = channel;

        // Create responder
        var responderOn = MIDIFunc.noteOn(
            callbackFunctionOn ? {|val,chan|
                "% (note  % chan %): % ON".format(label, noteNum, chan, val).postln;
            },
            noteNum,
            chan
        );

        var responderOff = MIDIFunc.noteOff(
            callbackFunctionOff ? {|val,chan|
                "% (note  % chan %): % OFF".format(label, noteNum, chan, val).postln;
            },
            noteNum,
            chan
        );

        "Creating Storks responder %".format(label).postln;

        // TODO:
        // responders[channel][layer][button] = responderOn;
    }
	// Callbacks receive the arguments val and chan
	// Callback function can be either a function or a collection of functions. In the latter case, all the collected functions will be passed val and chan as arguments.
	registerEnc{|channel, layer, encoder, callbackFunction|
		var label = "storks_chan%_layer%_enc%".format(channel, layer, encoder).asSymbol;
		var offset = layer * numEncoders;
		var cc1 = encoder + offset;
		var cc2 = cc1 + 32;
		var chan = channel;

		// Create responder
		var responder = CC14.new(
			cc1,
			cc2,
			chan,
            fix: false
		);

		"Creating Storks responder %".format(label).postln;

		// Register callback function
		if(callbackFunction.isNil, {
			responder.func_({|val,chan|
				"% (cc %/% chan %): %".format(label, cc1, cc2, chan, val).postln;
			})
		}, {
			// Register function
			if(callbackFunction.class == Function, {
				responder.func_(callbackFunction)
			}, {
				// Register collection of funtions
				if(callbackFunction.isKindOf(Collection), {
					responder.func_({|val, chan|
						callbackFunction.do{|funcItem|
							funcItem.value(val, chan)
						}
					})
				})
			})
		});

		responders[channel][layer][encoder] = responder;
	}

	get{|channel, layer, encoder|
		^responders[channel][layer][encoder]
	}

	connect{
		if(MIDIClient.initialized.not, {
			"MIDIClient not initialized... initializing now".postln;
			MIDIClient.init;
		});

		MIDIClient.sources.do{|src, srcNum|
			if(src.device == "STORKS", {
                if(try{MIDIIn.isStorksConnected}.isNil, {
                    if(MIDIClient.sources.any({|e| e.device=="STORKS"}), {
			    	"Connecting STORKS %".format(srcNum).postln;
                        MIDIIn.connect(srcNum, src).addUniqueMethod(\isStorksConnected, {true});
                    });
                }, {"STORKS is already connected... (device is busy)".postln});
			});
		}
	}
}
