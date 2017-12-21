package nu.mine.mosher.gedcom;

@SuppressWarnings({"access", "WeakerAccess", "unused"})
public class GedcomDupPtrOptions extends GedcomOptions {
    public void help() {
        this.help = true;
        System.err.println("Usage: gedcom-dupptr [OPTIONS] <in.ged");
        System.err.println("Options:");
        options();
    }

    public GedcomDupPtrOptions verify() {
        if (this.help) {
            return this;
        }
        return this;
    }
}
