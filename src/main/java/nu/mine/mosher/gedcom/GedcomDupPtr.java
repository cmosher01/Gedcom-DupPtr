package nu.mine.mosher.gedcom;

import nu.mine.mosher.collection.TreeNode;
import nu.mine.mosher.gedcom.exception.InvalidLevel;
import nu.mine.mosher.mopper.ArgParser;

import java.io.IOException;
import java.util.*;

import static nu.mine.mosher.logging.Jul.log;

// Created by Christopher Alan Mosher on 2017-09-08

public class GedcomDupPtr implements Gedcom.Processor {
    private final GedcomDupPtrOptions options;



    public static void main(final String... args) throws InvalidLevel, IOException {
        log();
        final GedcomDupPtrOptions options = new ArgParser<>(new GedcomDupPtrOptions()).parse(args).verify();
        new Gedcom(options, new GedcomDupPtr(options)).main();
        System.out.flush();
        System.err.flush();
    }



    private GedcomDupPtr(final GedcomDupPtrOptions options) {
        this.options = options;
    }



    //    @Override
    //    public boolean process(final GedcomTree tree) {
    //        tree.getRoot().forAll(n -> {
    //            final GedcomLine line = n.getObject();
    //            final Map<String, Integer> mapCountByTag = new HashMap<>(4);
    //            n.forEach(c -> {
    //                final GedcomLine lineC = c.getObject();
    //                if (lineC.getTagString().equals("_APID")) {
    //                    mapCountByTag.merge("_APID", 1, (o, v) -> o + v);
    //                }
    //            });
    //            mapCountByTag.forEach((String k, Integer v) -> {
    //                if (v > 1) {
    //                    System.out.println(findContainingRecord(n).getObject().getID());
    //                    System.out.println("    "+line);
    //                    System.out.println("        " + k + ": " + v);
    //                }
    //            });
    //        });
    //        return false;
    //    }
    //
    //    private static TreeNode<GedcomLine> findContainingRecord(final TreeNode<GedcomLine> n) {
    //        if (n.parent().parent() == null) {
    //            return n;
    //        }
    //        return findContainingRecord(n.parent());
    //    }



    @Override
    public boolean process(final GedcomTree tree) {
        final Map<String, Integer> mapSourIdWithApid = new HashMap<>(512);
        final Map<String, Integer> mapSourIdWithoutApid = new HashMap<>(512);
        final Map<String, String> mapSourIdToRepoId = new HashMap<>(512);
        tree.getRoot().forAll(n -> {
            final GedcomLine line = n.getObject();
            if (line != null && line.getTag().equals(GedcomTag.SOUR)) {
                if (line.isPointer()) {
                    (hasChild(n, "_APID") ? mapSourIdWithApid : mapSourIdWithoutApid).merge(line.getPointer(), 1, (o, v) -> o + v);
                } else {
                    mapSourIdToRepoId.put(line.getID(), getChildPointer(n, "REPO"));
                }
            }
        });

        mapSourIdToRepoId.forEach((k, v) -> {
            final String repo = display(v, tree);
            if (!repo.isEmpty()) {
                final String sour = display(k, tree);
                System.out.printf(
                    "%4d,%4d,%s,%s\n",
                    Optional.ofNullable(mapSourIdWithoutApid.get(k)).orElse(0),
                    Optional.ofNullable(mapSourIdWithApid.get(k)).orElse(0),
                    repo,
                    sour);
            }
        });
        return false;
    }

    private String display(final String id, final GedcomTree tree) {
        String r = id;
        final TreeNode<GedcomLine> n = tree.getNode(id);
        if (n != null) {
            for (final TreeNode<GedcomLine> c : n) {
                final GedcomLine line = c.getObject();
                if (line.getTag().equals(GedcomTag.NAME) || line.getTag().equals(GedcomTag.TITL)) {
                    return line.getValue();
                }
            }
        }
        return r;
    }

    private String getChildPointer(final TreeNode<GedcomLine> n, final String tag) {
        for (final TreeNode<GedcomLine> c : n) {
            final GedcomLine line = c.getObject();
            if (line.getTagString().equals(tag) && line.isPointer()) {
                return line.getPointer();
            }
        }
        return "";
    }

    private boolean hasChild(final TreeNode<GedcomLine> n, final String tag) {
        for (final TreeNode<GedcomLine> c : n) {
            if (c.getObject().getTagString().equals(tag)) {
                return true;
            }
        }
        return false;
    }
}
