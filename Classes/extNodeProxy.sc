/*
+NodeProxy{
	hakkeMatrix{|hakkebraet, layer=0, page=0|
		var matrix = HakkeMappingMatrix.instances[this.key];
		if(matrix.isNil, {
			HakkeMappingMatrix.new(this, hakkebraet, layer, page)
		}, {
			matrix.openWindow();
		})
	}
}
*/
