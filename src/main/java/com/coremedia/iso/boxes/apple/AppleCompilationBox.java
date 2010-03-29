package com.coremedia.iso.boxes.apple;

/**
 * Compilation.
 */
public final class AppleCompilationBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "cpil";


  public AppleCompilationBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Compilation Box";
  }

  public int getCompilation() {
    return appleDataBox.getContent()[0];
  }

  public void setCompilation(int compilation) {
    appleDataBox = new AppleDataBox();
    appleDataBox.setVersion(0);
    appleDataBox.setFlags(21);
    appleDataBox.setFourBytes(new byte[4]);
    appleDataBox.setContent(new byte[]{(byte) (compilation & 0xFF)});

  }
}