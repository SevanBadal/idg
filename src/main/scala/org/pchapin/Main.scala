package org.pchapin

import java.io._
import scala.util.Random
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import java.io.PrintWriter;

object Main {

  val randomGenerator = new Random(0)
  val conf = new Configuration()
  val fs= FileSystem.get(conf)

  /**
    * Compute a random three dimensional position inside a sphere with the given radius. This
    * method evolves the state of the given random number generator.
    *
    * @return The (x, y, z) coordinates of the point.
    */
  def generate3DPosition(radius: Double, generator: Random): (Double, Double, Double) = {
    var xCoordinate = 0.0
    var yCoordinate = 0.0
    var zCoordinate = 0.0
    var distance = Double.MaxValue

    // Only 52.4% (pi/6) of the random points in a cube are also in the enclosed sphere.
    // This loop keeps retrying until a point in the enclosed sphere is found. It should
    // be rare for it to loop more than a few times.
    while (distance > radius) {
      xCoordinate = (2.0 * radius * generator.nextDouble()) - radius
      yCoordinate = (2.0 * radius * generator.nextDouble()) - radius
      zCoordinate = (2.0 * radius * generator.nextDouble()) - radius
      distance =
        Math.sqrt(xCoordinate * xCoordinate +
          yCoordinate * yCoordinate +
          zCoordinate * zCoordinate)
    }
    (xCoordinate, yCoordinate, zCoordinate)
  }


  /**
    * Generates random stars in a given region of space. This method writes a file containing
    * information about each star.
    *
    * @param count The number of stars to generate.
    * @param radius The maximum distance of any generated star.
    * @param outputName The name of the output file holding a table of data about the stars.
    * @return An array of generated star objects.
    */
  def generateStars(count: Int, radius: Double, outputName: String): Array[Star] = {
    val stars = Array.ofDim[Star](count)
    for (i <- stars.indices) {
      stars(i) = Star(generate3DPosition(radius, randomGenerator))
    }

    // Output the stellar data for reference.
    val output = fs.create(new Path(outputName))
    val starWriter = new PrintWriter(output)
    for (i <- stars.indices) {
      val Star((x, y, z)) = stars(i)
      val coordinatePicture = "%+010.5f"
      val formattedX = coordinatePicture.format(x)
      val formattedY = coordinatePicture.format(y)
      val formattedZ = coordinatePicture.format(z)
      val formattedStarID = "%08d".format(i)
      starWriter.write(s"$formattedStarID,$formattedX,$formattedY,$formattedZ")
      starWriter.write("\n")
    }
    starWriter.close()
    stars
  }


  def main(args: Array[String]): Unit = {

    val programStatus =
      if (args.length != 1) {
        println("Usage: IDG star-count")
        1  // Failure
      }
      else {
        val radiansToDegrees = 360.0 / (2.0 * Math.PI)
        val earthSunBaseline = 1.496e8 / (2.998e5 * 86400.0 * 365.25)  // In light years.
        val stars = generateStars(args(0).toInt, 1000.0, "idg/stars.txt")

        // Generate the imaginary data.
        val output = fs.create(new Path("idg/observations.txt"))
        val observationWriter = new PrintWriter(output)
        println("Generating observations for day...")
        for (dayNumber <- 1 to 365) {
          print(s"\r$dayNumber")
          for (i <- stars.indices) {
            val formattedDayNumber = "%03d".format(dayNumber)
            val formattedStarID = "%08d".format(i)
            val Star((x, y, z)) = stars(i)

            // Compute the nominal position.
            val eclipticPlaneDistance = Math.sqrt(x*x + y*y)
            val eclipticLongitude = radiansToDegrees * Math.atan2(y, x)
            val eclipticLatitude = radiansToDegrees * Math.atan(z/eclipticPlaneDistance)

            // Compute parallax deviation.
            val distance = Math.sqrt(x*x + y*y + z*z)
            val parallaxDeviation = radiansToDegrees * Math.atan(earthSunBaseline/distance)

            // Compute the parallax shift (assumes a circular shift)
            val yearAngle = 2.0 * Math.PI * dayNumber.toDouble / 365.0  // In radians
            val adjustedLongitude = eclipticLongitude + parallaxDeviation * Math.cos(yearAngle)
            val adjustedLatitude = eclipticLatitude + parallaxDeviation * Math.sin(yearAngle)

            val formattedLongitude = "%+014.9f".format(adjustedLongitude)
            val formattedLatitude = "%+013.9f".format(adjustedLatitude)
            observationWriter.write(s"$formattedDayNumber,$formattedStarID,$formattedLongitude,$formattedLatitude")
            observationWriter.write("\n")
          }
        }
        println("\nDone!")
        observationWriter.close()
        0  // Success
      }

    System.exit(programStatus)
  }

}
