package mobac.program.tiledatawriter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import mobac.optional.JavaAdvancedImaging;

public class TileImageJpegInverseDataWriter extends TileImageJpegDataWriter {
	public TileImageJpegInverseDataWriter(double jpegCompressionLevel) {
		super(jpegCompressionLevel);
	}

	public TileImageJpegInverseDataWriter(float jpegCompressionLevel) {
		super(jpegCompressionLevel);
	}

	public TileImageJpegInverseDataWriter(TileImageJpegDataWriter jpegWriter) {
		super(jpegWriter.getJpegCompressionLevel());
	}

	@Override
	public void processImage(BufferedImage image, OutputStream out) throws IOException {
		image = JavaAdvancedImaging.invert(image);
		super.processImage(image, out);
	}
}
