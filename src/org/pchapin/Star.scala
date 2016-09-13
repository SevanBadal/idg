package org.pchapin

/**
  * Stars have a physical location in space (given as (x,y,z) coordinates in units of light
  * years). For this program, that location is assumed to be fixed and unchanging. This is,
  * of course, not realistic. A future version of IDG may remove this limitation.
  *
  * @param physicalLocation The (x,y,z) coordinates of the star in light years.
  */
case class Star(physicalLocation: (Double, Double, Double))
