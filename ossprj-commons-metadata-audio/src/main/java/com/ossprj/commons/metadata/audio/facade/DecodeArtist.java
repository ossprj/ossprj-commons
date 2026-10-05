package com.ossprj.commons.metadata.audio.facade;


import com.ossprj.commons.metadata.audio.model.Artist;

import java.util.*;
import java.util.stream.Collectors;

public class DecodeArtist {

    private List<Artist> artists = Arrays.stream((
            "aas,Acoustic Allstars\n" +
            "abb,Allman Brothers Band\n" +
            "amf,Amfibian\n" +
            "bc,Black Crowes\n" +
            "beanland,Beanland\n" +
            "bf,Blue Floyd\n" +
            "bhic,Ben Harper & The Innocent Criminals\n" +
            "bmelon,Blind Melon\n" +
            "bnb,Burt Neilson Band\n" +
            "cc,Counting Crows\n" +
            "ch,Charlie Hunter\n" +
            "db,Disco Biscuits\n" +
            "dbb,Deep Banana Blackout\n" +
            "dbr,Day by the River\n" +
            "dg5t,David Grisman Quintet\n" +
            "dmb,Dave Matthews Band\n" +
            "DSO,Dark Star Orchestra\n" +
            "gal,Galactic\n" +
            "gat,Garage A Trois\n" +
            "gba,Greyboy Allstars\n" +
            "gd,Grateful Dead\n" +
            "glove,G. Love & Special Sauce\n" +
            "gsw,God Street Wine\n" +
            "guster,Guster\n" +
            "hday,Howie Day\n" +
            "ho,Schleigho\n" +
            "ht,Hot Tuna\n" +
            "jdouglas,Jerry Douglas\n" +
            "jid,Jazz Is Dead\n" +
            "jk,Jorma Kaukonen\n" +
            "JMP,Jazz Mandolin Project\n" +
            "jp,Jemimah Puddleduck\n" +
            "jsb,John Scofield Band\n" +
            "kdtu,Karl Denson's Tiny Universe\n" +
            "kw,Keller Williams\n" +
            "ld,Living Daylights\n" +
            "lf,Little Feat\n" +
            "ll,Los Lobos\n" +
            "LoS,Leftover Salmon\n" +
            "lp,Latin Playboys\n" +
            "lt,Lake Trout\n" +
            "md,Miles Davis\n" +
            "mmf,Missing Man Formation\n" +
            "moe,moe.\n" +
            "mule,Gov't Mule\n" +
            "nrps,New Riders of the Purple Sage\n" +
            "ny,Neil Young\n" +
            "oh,Oysterhead\n" +
            "osp,Ominous Seapods\n" +
            "percy,Percy Hill\n" +
            "pf,Pink Floyd\n" +
            "phil,Phil Lesh & Friends\n" +
            "ph,Phish\n" +
            "PMB,Pat McGee Band\n" +
            "primus,Primus\n" +
            "qms,Quicksilver Messenger Service\n" +
            "rad,Radiators\n" +
            "ratdog,Ratdog\n" +
            "recipe,The Recipe\n" +
            "rh,Radiohead\n" +
            "rr,Rusted Root\n" +
            "rwtc,Robert Walter's 20th Congress\n" +
            "sf,Strangefolk\n" +
            "skb,Steve Kimock Band\n" +
            "slip,The Slip\n" +
            "soulive,Soulive\n" +
            "sub,Sublime\n" +
            "th,Talking Heads\n" +
            "tr,Tim Reynolds\n" +
            "traffic,Traffic\n" +
            "trey,Trey Anastasio\n" +
            "um,Umphrey's McGee\n" +
            "vh,Vertical Horizon\n" +
            "vinyl,Vinyl\n" +
            "vw,Victor Wooten\n" +
            "wb4t,Will Bernard 4tet\n" +
            "ween,Ween\n" +
            "wh,Warren Haynes\n" +
            "wsp,Widespread Panic\n" +
            "wu,The Big Wu\n" +
            "ymsb,Yonder Mountain String Band\n" +
            "zero,Zero\n").split("\n")).map(line -> {
                final String[] elements = line.split(",");
                return new Artist(elements[0],elements[1]);
    }).collect(Collectors.toList());

    private String normalizeArtistName(final String name) {
        return name
                .replaceAll(" ", "")
                .replaceAll("_", "")
                .replaceAll("&", "and")
                .toLowerCase();
    }

    private Map<String, Artist> artistsByName = new HashMap<>();

    {
        artists.forEach(artist -> {
            final String normalizedArtistName = normalizeArtistName(artist.getArtistName());
            artistsByName.put(artist.getArtistAbbreviation(), artist);
            artistsByName.put(normalizedArtistName, artist);
        });
    }

    public Optional<Artist> perform(final String name) {
        final String artistNameNormalized = normalizeArtistName(name);
        if (artistsByName.containsKey(artistNameNormalized)) {
            return Optional.of(artistsByName.get(artistNameNormalized));
        }
        return Optional.empty();
    }

    public static void main(String[] args) {
        final DecodeArtist decodeArtist = new DecodeArtist();

        System.out.println(decodeArtist.perform("gd"));
        System.out.println(decodeArtist.perform("ge"));
        System.out.println(decodeArtist.perform(" Grateful  Dead  "));
    }

}
