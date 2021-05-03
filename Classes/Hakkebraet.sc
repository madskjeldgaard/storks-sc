/*

@TODO: 7 bit midi functionality

*/
Hakkebraet {
	var <responders;

	var <numLayers = 16;
	var <numPages = 8;
	var <numEncoders = 8;

	var <maxMidiVal14Bit = 16363;
	var <maxMidiVal7Bit = 127;

	var scale;

	*new { |fourteenBit=true, connectOnInit=true, registerDefaults=true, scaledValues=true|
		^super.new.init(fourteenBit, connectOnInit, registerDefaults, scaledValues);
	}

	init { | fourteenBit, connectOnInit, registerDefaults, scaledValues|
		responders = numLayers.collect{|layNum|
			numPages.collect{|pageNum|
				Array.newClear(numEncoders)
			}
		};

		scale = scaledValues;

		if(connectOnInit, { 
			this.connect();
		});

		if(registerDefaults, { 
			numLayers.do{|layerNum|
				numPages.do{|pageNum|
					numEncoders.do{|encNum|
						this.register(layerNum, pageNum, encNum)
					}
				}
			};
		});

		^this
	}

	// Callbacks receive the arguments val and chan
	// Callback function can be either a function or a collection of functions. In the latter case, all the collected functions will be passed val and chan as arguments.
	register{|layer, page, encoder, callbackFunction|
		var label = "h_layer%_page%_enc%".format(layer, page, encoder).asSymbol;
		var offset = page * numEncoders;
		var cc1 = encoder + offset;
		var cc2 = cc1 + 32;
		var chan = layer;

		// Create responder
		var responder = H14BCC.new(
			label,
			cc1,
			cc2,
			chan
		);
		
		"Creating Hakkebraet responder %".format(label).postln;

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

		responders[layer][page][encoder] = responder;
	}

	get{|layer, page, encoder|
		^responders[layer][page][encoder]
	}

	connect{
		if(MIDIClient.initialized.not,{
			"MIDIClient not initialized... initializing now".postln;
			MIDIClient.init;
		});

		MIDIClient.sources.do{|src, srcNum| 
			if(src.device == "Hakkebraet", {
				"Connecting Hakkebraet %".format(srcNum).postln;
				MIDIIn.connect(srcNum, src)
			});
		}
	}
}

// From Carl Testa's FourteenBitCC class
H14BCC {
	var <>label;
	var <>cc1;
	var <>cc2;
	var <>chan;
	var <>func;
	var msb;
	var lsb;
	var value;

	*new {arg label, cc1 , cc2 , chan, fix=true;
		^super.new.init(label, cc1, cc2, chan, fix)
	}

	init { arg aLabel, aCc1, aCc2, aChan, fix;
		var def;

		label = aLabel;
		cc1 = aCc1;
		cc2 = aCc2;
		chan = aChan;

		def = MIDIdef.cc(label.asSymbol, {
			|val,num,chan,src|
			case
			{num==cc1}{this.msbSet(val)}
			{num==cc2}{this.lsbSet(val)}
		}, [cc1,cc2],chan);

		if(fix, { def.fix; })
	}

	msbSet { arg byte;
		msb = byte;
		this.check;
	}

	lsbSet { arg byte;
		lsb = byte;
		this.check;
	}

	check {
		if(lsb.notNil and: { msb.notNil }) {
			value = (msb << 7 + lsb);
			func.(value,chan);
			msb = nil;
			lsb = nil;
		}
	}
}


