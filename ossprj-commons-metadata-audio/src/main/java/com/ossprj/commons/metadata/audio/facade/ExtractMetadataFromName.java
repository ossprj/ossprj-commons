package com.ossprj.commons.metadata.audio.facade;

import com.ossprj.commons.metadata.audio.model.ExtractedTags;
import com.ossprj.commons.metadata.audio.model.Metadata;
import com.ossprj.commons.metadata.audio.model.PatternWithTransformation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractMetadataFromName {

    private final ExtractTagsFromName extractTagsFromName = new ExtractTagsFromName();

    private final List<PatternWithTransformation> patterns = Arrays.asList(
            // 4 digit year, dashes
            new PatternWithTransformation(Pattern.compile("^(.*?)\\.?(\\d{4}-\\d{2}-\\d{2})\\.?(.*?$)"), Optional.empty()),
            // 4 digit year, periods, transforms periods into dashes
            new PatternWithTransformation(Pattern.compile("^(.*?)\\.?(\\d{4}\\.\\d{2}\\.\\d{2})\\.?(.*?$)"), Optional.of(s -> s.replaceAll("\\.", "-"))),

            // 2 digit year, dashes
            new PatternWithTransformation(Pattern.compile("^(.*?)\\.?(\\d{2}-\\d{2}-\\d{2})\\.?(.*?$)"), Optional.of(s -> {
                // if 2 digit year is less than 30 prefix with 19, otherwise prefix with 20
                return Integer.parseInt(s.substring(0, 2)) <= 30 ? "20" + s : "19" + s;
            })),
            // 2 digit year, periods, transforms periods into dashes
            new PatternWithTransformation(Pattern.compile("^(.*?)\\.?(\\d{2}\\.\\d{2}\\.\\d{2})\\.?(.*?$)"), Optional.of(s -> {
                final String replacedString = s.replaceAll("\\.", "-");
                // if 2 digit year is less than 30 prefix with 19, otherwise prefix with 20
                return Integer.parseInt(replacedString.substring(0, 2)) <= 30 ? "20" + replacedString : "19" + replacedString;
            }))
    );

    public Optional<Metadata> perform(final String name) {
        //System.out.println(name);
        for (final PatternWithTransformation p : patterns) {
            final Matcher matcher = p.getPattern().matcher(name);
            if (matcher.matches()) {
                final String artistName = matcher.group(1);
                final String date = matcher.group(2);
                final String data = matcher.group(3);

                // Extract any tags we can find
                final ExtractedTags extractedTags = extractTagsFromName.perform(data.trim());

                return Optional.of(new Metadata(
                        artistName.trim(),
                        p.getTransformation().isPresent() ? p.getTransformation().get().apply(date) : date,
                        extractedTags.getData().trim(),
                        extractedTags.getCleanedData(),
                        extractedTags.getTags()));
            }
        }

        return Optional.empty();
    }

    public static void main(String[] args) {
        final String names =
                "1968-1980 - Studio Magik Sessions 1968-1980 (Godfatherecords)\n" +
                        "1969\n" +
                        "1970-09-23 'Welcome to the Fillmore East' NET-TV Special, New York, NY\n" +
                        "19751214 (DS) ELVIS PRESLEY Las Vegas (Rejuvenated & Rockin')\n" +
                        "1976-03-29_afternoonshow_queen\n" +
                        "1976-03-29_eveningshow_queen\n" +
                        "1978-12-01_queen\n" +
                        "1979-12-26_queen\n" +
                        "1985-05-15_queen_nolabel\n" +
                        "1985-05-15_queen_wardour031\n" +
                        "1986-07-30_queen\n" +
                        "1994-04-29_30 Kulturbolaget, Malm\u009D, Sweden, aud (6 discs) B+\n" +
                        "2017-06-23 - KMHB - Essen BD25\n" +
                        "2018-12-01 - KMHB - Karlsruhe DVD\n" +
                        "2018-12-28 Otis Grove\n" +
                        "2018-12-29 Barika\n" +
                        "2018-12-29-Oteil and friends-Port Chester, NY FLAC\n" +
                        "2018-12-31 Max Creek\n" +
                        "2018-12-31-Gov't mule-NYC FLAC\n" +
                        "54-40 2013-02-15 Maple Ridge, BC CA-11 - 16 BIT\n" +
                        "acb2018-12-31\n" +
                        "acidcats2018-12-22\n" +
                        "Aerosmith - 1984-12-12 - The Summit, Houston, TX (Pro Shot DVD)\n" +
                        "BA2018-12-29\n" +
                        "BBC In Concert - Neil Young 1971 [DVD5] (PAL)\n" +
                        "Bill Withers - BBC4 DVB - In Concert 1973 [DVD5] (PAL)\n" +
                        "Black Crowes 1990-06-21 NYC 2nd GEN - 16 BIT\n" +
                        "Black Crowes 1993-05-30 Nuremberg, Germany 1st GEN - 16 BIT\n" +
                        "BOB DYLAN - Yuzawa-cho, Niigata, Japan (29-July-2018) (DVDylan ID #2081) \n" +
                        "Bob Dylan 2018-11-29 New York Blu-ray ttd\n" +
                        "Bob Dylan Anaheim 2000-03-10 Early -LB-440\n" +
                        "Bob Dylan Cincinnati 1988-06-22 [LB-452]\n" +
                        "Bob Dylan D1137 2000.11.18 Tropicana Hotel & Casino Showroom, Atlantic City, NJ (Late show) \n" +
                        "Bob Dylan D1138 2000.11.18 Tropicana Hotel & Casino Showroom, Atlantic City, NJ (Early show)\n" +
                        "Bob Dylan D1139 2001.11.19 Madison Square Garden, New York City, NY TTD\n" +
                        "Bob Dylan D2063.2 1990.10.18 Beacon Theatre, New York City, NY TDD\n" +
                        "Bob Dylan D259.su 2003.08.12 Hammerstein Ballroom, New York City, NY REMAKE TTD\n" +
                        "Bob Dylan D260.su 2003.08.20 Hammerstein Ballroom, New York City, NY TTD\n" +
                        "Bob Dylan D264.su 2003.08.13 Hammerstein Ballroom, New York City, NY TTD\n" +
                        "Bob Dylan Goteberg 2001-06-29 cc-LB-420\n" +
                        "Bob Dylan Innsbruck 1991-06-14 LB-444\n" +
                        "Bob Weir and Wolf Bros 2018-11-18 Beacon Theater New York, NY {Mr. Railing}\n" +
                        "Bob Weir and Wolf Bros 2018-11-19 Beacon Theater New York, NY {Mr. Railing}\n" +
                        "Bon Jovi - Sessions 1-4\n" +
                        "Bon Jovi - Sessions 5-8\n" +
                        "BottleRockets2018-11-16.JoE_BohnerT\n" +
                        "brosisjam2018-12-30.spyder9.flac16\n" +
                        "Bruce Hornsby - 1998-08-13\n" +
                        "BS2018-12-31\n" +
                        "cabinet2018-12-31.litz.sbd.schoepsMK41v.flac16\n" +
                        "Calexico 2010-09-10 Mainz\n" +
                        "Call for the Priest - 2018-12-15 Club 861, Kenmore, NY\n" +
                        "CamperVanBeethoven2011-06-18.MarkLynn\n" +
                        "Carole King - BBC4 DVB - In Concert 1971 [DVD5] (PAL)\n" +
                        "cc2018-12-15.aud.flac\n" +
                        "ccr1971-07-04.fm.flac\n" +
                        "Cheap Trick 1999-12-31 Disney MGM Studios Krw_co Vhs Master XP Mode ttd\n" +
                        "cp2018-12-28.km184.flac16\n" +
                        "Cracker2011-06-17.MarkLynn\n" +
                        "Cracker2011-06-18.MarkLynn\n" +
                        "Cracker2018-12-27.Flac24\n" +
                        "cracker2018-12-28\n" +
                        "cracker2018-12-29.Matrix\n" +
                        "CS2018-12-31.RODENT5-ZOOMH5.16\n" +
                        "CSNY - 2002-03-09 Trump Taj Mahal, Atlantic City, NJ [TTD]\n" +
                        "CVB2018-12-27.Flac24\n" +
                        "CVB2018-12-28\n" +
                        "cvb2018-12-29.Matrix\n" +
                        "CWE2018-12-31.Pabst\n" +
                        "David Allan Coe 2018-12-15 Highland, IN - 16 BIT\n" +
                        "David Bowie - Paris '87 16-44\n" +
                        "David Crosby and Graham Nash 1970-11-19 BBC In Concert Satellite Rebroadcast krw_co xp maste\n" +
                        "David Lindley 1989-11-29 Victoria, BC - 16 BIT\n" +
                        "David Murray 2018-10-04.mk5.perks.flac16\n" +
                        "DavidLowery2011-06-18.MarkLynn\n" +
                        "December29history_ET\n" +
                        "December30history_ET\n" +
                        "December31history_ET\n" +
                        "Deep Purple 1973-05-24 Music Hall, Boston, MA Maloney Master 16 44.1 ttd\n" +
                        "Deep Purple 1973-05-24 Music Hall, Boston, MA Maloney Master 24 96 ttd\n" +
                        "dellamae2018-10-06.akg-c480b.tetzeli.flac16\n" +
                        "Dick Dale - 1997-04-12 Graffiti Lounge, Pittsburgh, PA [TTD]\n" +
                        "Dick Dale - 1999-04-17 Graffiti Lounge, Pittsburgh, PA [TTD]\n" +
                        "doomflamingo218-12-28\n" +
                        "DR2018-12-29\n" +
                        "DR2018-28-18.DR2018-28-18\n" +
                        "drbacon2018-09-22\n" +
                        "dubapocalypse2018-11-18aud2448\n" +
                        "dubapocalypse2018-12-08.dpa4023.flac2448\n" +
                        "dubapocalypse2018-12-08aud2448\n" +
                        "dubapocalypse2018-12-09.dpa4023.flac2448\n" +
                        "dubapocalypse2018-12-09aud2448\n" +
                        "dwg2018-12-31.ck930.flac24\n" +
                        "evanescence2018-09-05.flac16\n" +
                        "FA2018-12-28._KM140-MTX\n" +
                        "Falling Joys 1991-02-07 Victoria, BC - 16 BIT\n" +
                        "fc2018-12-27.fc2018-12-27\n" +
                        "Fleetwood Mac- A Musical History\n" +
                        "Flying Burrito Brothers - 1969-07-27 - Seattle Pop, Woodinville, WA rm (JEMS.goody)\n" +
                        "fngsam2018-12-28.aud\n" +
                        "fruition2018-10-05.akg-c460b.tetzeli.flac16\n" +
                        "fruition2018-10-05.akg-c460b.tetzeli.flac16\n" +
                        "fruition2018-10-05.dr-40.etown.tetzeli.flac16\n" +
                        "fruition2018-10-05_etown.tascam_dr-40.tetzeli.flac16\n" +
                        "Fu Manchu - 2018-11-11 - Vancouver, BC [NTSC]\n" +
                        "garciapeoples2018-12-22\n" +
                        "Gary Hoey Live 2018-11-23 @ The Sellersville Theater\n" +
                        "garybackstrom2018-12-22.dpa4023-sbd.flac2448\n" +
                        "garybackstrom2018-12-22mtx2448\n" +
                        "GBB2018-12-27\n" +
                        "GBB2018-29-12.GBB2018-29-12_237\n" +
                        "gd1970-03-21.144383.late.rec3.m-aud.flac1644\n" +
                        "gd1980-08-31.144288.fob.nak700.cohen.miller.clugston.flac1648\n" +
                        "gd1980-08-31.144289.fob.nak700.cohen.miller.clugston.flac2496\n" +
                        "gd1981-12-02.144276.fob.senn421.streeter.miller.clugston.flac1648\n" +
                        "gd1981-12-02.144277.fob.senn421.streeter.miller.clugston.flac2496\n" +
                        "gd1984-12-28.set2.bhp.dvd9\n" +
                        "gd1984-12-29.bhp,dvdf\n" +
                        "gd1984-12-31.bhp.dvdf\n" +
                        "gd1986-12-27.bhp.bd\n" +
                        "gd1986-12-27.set2-alt angle.bhp.dvd9\n" +
                        "gd1987-07-12.144319.set3.mtx.tobin.flac1644\n" +
                        "gd1987-07-12.144320.set3.dts.tobin.flac1644\n" +
                        "gd1987-07-12.144321.set3.5-1.tobin.flac1648\n" +
                        "gd1989-12-27.bhp.dvdf\n" +
                        "gd1989-12-30.set1.bhp.dvd5\n" +
                        "gd1991-12-28.alt-angle.bhp.dvdf\n" +
                        "gd1992-06-18.144379.nak300-cp4.MarkLynn.flac1648\n" +
                        "gd1992-12-03.set2-aud.bhp.dvd9\n" +
                        "gd1992-12-16.set2.bhp.dvd9\n" +
                        "gd1992-12-17.set2.bhp.dvd9\n" +
                        "gd1994-12-18.bhp.dvdf\n" +
                        "gd1994-12-19.bhp.dvd9\n" +
                        "gd1995-02-20.144300.sbd.miller.clugston.flac1644\n" +
                        "gd87-09-23.090559.nak300.damico.sbeok.t-flac16\n" +
                        "gd87-09-23.137228.mtx.tobin.flac16\n" +
                        "gd87-09-23.137420.sbd-UltraMatrix.cm.miller.t-flac16\n" +
                        "Gene Clark - 1990-02-03 McCabe's Guitar Shop, Santa Monica, CA with Carla Olson [TTD]\n" +
                        "George Michael 1996-10-11 Three Mills Island Studios, London, UK ('MTV Unplugged') [VHS]\n" +
                        "Ghost - 2018-12-08 Sony Center for the Performing Arts, Toronto, ONT [16-44]\n" +
                        "Ghost - 2018-12-08 Sony Center for the Performing Arts, Toronto, ONT [24-96]\n" +
                        "GM2018-12-31.flac16\n" +
                        "GM2018-12-31.flac24\n" +
                        "GratefulDead1992-06-18-MarkLynn-FLAC1648\n" +
                        "gsbg2018-10-05.akg-c460b.tetzeli.flac16\n" +
                        "gsbg2018-12-28\n" +
                        "gsbg2018-12-28.Neumann\n" +
                        "gsbg2018-12-29\n" +
                        "gsbg2018-12-29.Neumann\n" +
                        "gsbg2018-12-30\n" +
                        "gsbg2018-12-30.Neumann\n" +
                        "gsbg2018-12-31\n" +
                        "gsbg2018-12-31.Neumann\n" +
                        "guitartrio1980-12-05.dvdf\n" +
                        "GWDR.2018-09-15.Stiefel.Salina\n" +
                        "hackensawboys2018-10-05.akg-c460b.tetzeli.flac16\n" +
                        "Haiku - 2018-10-03 Sportsmen's Tavern, Buffalo, NY [16-44]\n" +
                        "Haiku - 2018-10-03 Sportsmen's Tavern, Buffalo, NY [24-48]\n" +
                        "Hedberg04.11.13\n" +
                        "Hexbelt1992-03-27.SBDAKG414matrix\n" +
                        "HHG2018-12-31.Pabst\n" +
                        "hjp2018-12-28.cmc65xt.sbd.matrix.flac16\n" +
                        "hjp2018-12-28.cmc65xt.sbd.matrix.flac24\n" +
                        "honker2018-12-28\n" +
                        "Hyryder2018-12-30.sbd.flac16\n" +
                        "Janam2018-12-22.flacf\n" +
                        "jeff2018-12-30.flac16\n" +
                        "Jerry's Middle Finger 2018-12-31 Golden Sails, Long Beach, CA\n" +
                        "Jesus & Mary Chain - 2014-06-31 Dublin, Vicar Street TTD\n" +
                        "Jesus & Mary Chain - 2014-11-21 Glasgow, Barrowlands Ballroom (Incomplet) TTD\n" +
                        "Jesus & Mary Chain - 2016-03-11 Londres, Forum TTD\n" +
                        "Jethro Tull - 1999-06-15 - Baden Baden, Germany (German TV) (DVB, Pro Shot DVD) ttd\n" +
                        "jh2018-12-28\n" +
                        "jh2018-12-29\n" +
                        "jimiller2018-11-22\n" +
                        "jjj2018-12-22.sbd.at835.matrix.flac24\n" +
                        "JJJ2018-12-29\n" +
                        "jjj2018-12-30\n" +
                        "JKOB2018-12-29.sbd.nico11104.flac24\n" +
                        "Joan Armatrading-1990-07-05-The Royal Centre, Nottingham, England (24bit & 16bit)\n" +
                        "Joan Baez - 1988-07-21 Villa Medicea di Poggio a Caiano, Italy (2nd gen) [TTD]\n" +
                        "Joe Bonamassa 2013-04-10 Vancouver CA-11 - 16 BIT\n" +
                        "Joe Cocker 1991-07-13.ECV.DVD5\n" +
                        "johnmclaughlin1981-08-30dvd\n" +
                        "johnmclaughlin1987-02-19dvd\n" +
                        "johnmclaughlin1996-07-25dvd\n" +
                        "Jonathan Richman - 2014-11-15 Plainfield, Goddard College, Haybarn Theatre, Vermont TTD\n" +
                        "JSB2018-12-29.JohnSpignesiBandTowneTavernDecember2018\n" +
                        "kdtu2018-12-27.mg20.zoom.obaaron.2448\n" +
                        "kdtu2018-12-27.mg20.zoom.obaaron.2448\n" +
                        "Kiss 1976-08-20 anaheim pro shot ultimate edition ttd\n" +
                        "KL2018-07-28\n" +
                        "KungFu2018-12-21.SBD-Matrix-AKG483\n" +
                        "Lake Street Dive 2013-03-22 Vancouver CA-11 - 16 BIT\n" +
                        "lhb2008-07-13.nak700.flac16.remastered\n" +
                        "lilsmokies2018-10-05.akg-c460b.tetzeli.flac16\n" +
                        "lilsmokies2018-10-05.akg-c460b.tetzeli.flac16\n" +
                        "lilsmokies2018-10-05._etown.dr-40.tetzeli.flac16\n" +
                        "lilsmokies2018-10-05_etown.tascam_dr-40.tetzeli.flac16\n" +
                        "Lindsey Buckingham - 2018-11-28 Riviera Theater, North Tonawanda, NY [16-44]\n" +
                        "Lindsey Buckingham - 2018-11-28 Riviera Theater, North Tonawanda, NY [24-96]\n" +
                        "Little Steven & The Disciples of Soul - 1987-12-04 Issadion, Stockholm, Sweden (1st gen) [TT\n" +
                        "Live Under The Sky 1981 & 1983\n" +
                        "Live Under The Sky 1984 & 1985\n" +
                        "Live Under The Sky 1986 & 1987\n" +
                        "Live Under The Sky 1988 & 1989\n" +
                        "Live Under The Sky 1990\n" +
                        "Live Under The Sky 1991\n" +
                        "Live Under The Sky 1992\n" +
                        "LL2018-12-27 Santa Ana, CA\n" +
                        "LL2018-12-28 Santa Ana, CA\n" +
                        "Lotus Land - 2018-11-09 Riviera Theater, North Tonawanda, NY [16-44] bigdaddybflo\n" +
                        "Lotus Land - 2018-11-09 Riviera Theater, North Tonawanda, NY [24-48] bigdaddybflo\n" +
                        "lunarticks2018-11-29.lunarticks2018-11-29\n" +
                        "lunarticks2018-12-28\n" +
                        "lzrutre2018-12-07\n" +
                        "mb2018-12-29.akg461.matrix.flac24\n" +
                        "mc2003-12-12.DAT.Dave.44.1KHz.flac16\n" +
                        "mc2003-12-20.DAT.Dave.44.1KHz.flac16\n" +
                        "MC2018-12-31\n" +
                        "MC50 - 2018-10-17 - Vancouver, BC [NTSC]\n" +
                        "md1970-07-25.aud.lmpp217.flac16\n" +
                        "Mickey Hart's Mystery Box - 1996-06-26\n" +
                        "Minor Empire Live @ Rockwood Music Hall 2 - 20181120 [AudVid]\n" +
                        "MiZ2018-12-31.litz.sbd.schoepsMK41v.flac16\n" +
                        "mkb2018-07-19.mbhoka200.v3.flac16\n" +
                        "MKB2018-11-27.audFlac24\n" +
                        "MKB2018-12-30\n" +
                        "mkb2018-12-30.mkb2018-12-31\n" +
                        "moe.2008-01-26.akg414.flac\n" +
                        "moe.2008-01-26.ck1.flac\n" +
                        "moe.2018-12-30.dpa4011.flac24\n" +
                        "moe.2018-12-31.dpa4011.flac24\n" +
                        "mule2018-12-28.flac24\n" +
                        "NelsClineBillyMartinSkerikMonoNeon2018-12-29\n" +
                        "nevillejacobs2017-01-26.m10.spyder9.flac16\n" +
                        "Nicki Bluhm 2013-02-09 Vancouver CA-11\n" +
                        "nkcc2018-12-19.sbd.at835.matrix.flac24\n" +
                        "nma2018-12-29.akg483.flac16\n" +
                        "nma2018-12-29.akg483.flac16\n" +
                        "nma2018-12-29.akg483.flac24\n" +
                        "nma2018-12-29.akg483.flac24\n" +
                        "nma2018-12-30.ca14.flac16\n" +
                        "nmas2018-12-28.cmc65xt.flac16\n" +
                        "nmas2018-12-28.cmc65xt.flac24\n" +
                        "nmas2018-12-30.akgck61\n" +
                        "OBF2018-12-29.sbd.nico11104.flac24\n" +
                        "OBF2018-12-30.aud.8040.nico11104.flac24\n" +
                        "OBF2018-12-30.sbd.nico11104.flac24\n" +
                        "OBurbridge2018-12-29\n" +
                        "of2018-12-28\n" +
                        "OG2018-12-28\n" +
                        "Oteil2018-12-28.schoepsmk41v.Flac16\n" +
                        "oteilandfriends2018-12-29.mgmatrix\n" +
                        "oteilandfriends2018-12-30.mgmatrix\n" +
                        "otisgrove2018-12-28.414.462\n" +
                        "otisgrove2018-12-28.414.462.sbd.flac\n" +
                        "p.h.2018-06-22\n" +
                        "pappy2018-12-29.litz.sbd.akg414.flac16\n" +
                        "pfun2018-10-07.flac\n" +
                        "pgroove2018-12-30.peluso.flac24\n" +
                        "ph1993-08-06.144360.flac1648\n" +
                        "ph2018-06-22.flac\n" +
                        "ph2018-11-03.akg-c568eb_bsc1-hypers.logrippo.flac1648\n" +
                        "ph2018-12-28.akgmatrix.stearns.flac16\n" +
                        "ph2018-12-28.New.York.NY.padelimike.akg414.flac2496\n" +
                        "ph2018-12-29.akgmatrix.stearns.flac16\n" +
                        "ph2018-12-29.at4031.flac16\n" +
                        "ph2018-12-29.New.York.NY.padelimike.akg414.flac2496\n" +
                        "ph2018-12-30.akgmatrix.stearns.flac16\n" +
                        "ph2018-12-30.at4031.flac16\n" +
                        "ph2018-12-30.New.York.NY.padelimike.akg414.flac1648\n" +
                        "ph2018-12-30.New.York.NY.padelimike.akg414.flac2496\n" +
                        "ph2018-12-31.at4031.flac16\n" +
                        "ph2018.12.30.mk21.flac16\n" +
                        "ph2018.12.30.mk21.flac24\n" +
                        "ph2018.12.31.mk21.flac16\n" +
                        "ph2018.12.31.mk21.flac24\n" +
                        "Phil Collins - Live USA 1983-07-01\n" +
                        "Phish 16-44 Madison Square Garden NYC 2018-12-28\n" +
                        "phish 2018-12-28\n" +
                        "Phish Madison Square Garden NYC 2018-12-28\n" +
                        "Phish2018-12-28 NY ECM\n" +
                        "Phish2018-12-29 NY ECM\n" +
                        "Phish2018-12-30 NY ECM\n" +
                        "phish2018.12.29.schoepsmk22.flac16f\n" +
                        "phish2018.12.29.schoepsmk22.flac24f\n" +
                        "phish2018.12.29.schoepsmk3+mk22.flac16f\n" +
                        "phish2018.12.29.schoepsmk3.flac16f\n" +
                        "phish2018.12.29.schoepsmk3.flac24f\n" +
                        "phish2018.12.29.schoepsmk41v.flac16f\n" +
                        "phish2018.12.29.schoepsmk41v.flac24f\n" +
                        "Phish2018.12.30.schoepsmk22+mk3.flac16f\n" +
                        "phish2018.12.30.schoepsmk22.flac16f\n" +
                        "phish2018.12.30.schoepsmk22.flac24f\n" +
                        "phish2018.12.30.schoepsmk3.flac24f\n" +
                        "phish2018.12.30.schoepsmk41v.flac16f\n" +
                        "phish2018.12.30.schoepsmk41v.flac24f\n" +
                        "phish2018.12.31.schoepsmk22.flac16f\n" +
                        "phish2018.12.31.schoepsmk22.flac24f\n" +
                        "phish2018.12.31.schoepsmk3.flac24f\n" +
                        "phish2018.12.31.schoepsmk41v.flac16f\n" +
                        "phish2018.12.31.schoepsmk41v.flac24f\n" +
                        "Pink Floyd - 1994-09-13 - The Grass Was Greener (IFWT-DVD-035)\n" +
                        "Point Blank\n" +
                        "Popa Chubby - 2018-12-13 The Tralf, Buffalo, NY [16-44]\n" +
                        "Popa Chubby - 2018-12-13 The Tralf, Buffalo, NY [24-48]\n" +
                        "Primus - 2008-07-04 - Ranch Arena, Rothbury Music Festival, Rothbury, MI (2-Cam DVD) ttd\n" +
                        "psylojoe2018-09-21.flac24\n" +
                        "PTF2018-12-30.SBD.Matrix.Flac16\n" +
                        "ptosh1983-12-30.sbd.flac\n" +
                        "Queen 1977-06-06 Earl's Court London England Pro Shot PAL\n" +
                        "Queen 1979 Live Killers Tour 4 DVD Box Set PAL DVD\n" +
                        "Rainbow 1981-06-10 Essen, Germany VHS-x ttd\n" +
                        "Ratdog & Furthur Finale Jam - 1996-07-18\n" +
                        "RD1996-07-18\n" +
                        "REM 1983-11-09 Rhythmic Studios demos\n" +
                        "RGB2018-11-10.RobGlassmanBandMulligansNovember2018\n" +
                        "RGB2018-12-01.RobGlassmanBandHolidayShow2018\n" +
                        "rh2018-12-30\n" +
                        "rhitchcock2018-12-27.flac16\n" +
                        "Robert Plant 1993-11-23 Montreal, Quebec 1st Gen VHS to DVD ntsc krw_co ttd\n" +
                        "Robert Randolph 2012-06-30 Vancouver CA-11 - 16 BIT\n" +
                        "Roger Waters 1999-08-25 Grand Rapids, MI 1st Gen VHS to DVD krw_co ttd\n" +
                        "Roger Waters 2000-06-16 Phoenix, AZ vhs 1st gen to dvd krw_co ttd\n" +
                        "Rolling Stones 1997-09-18 Chicago, IL Pro Shot VHS to DVD ntsc krw_co ttd\n" +
                        "RRE2018-12-29.akgc568eb.mixpre6.flac24\n" +
                        "Rush - 1996-11-03 Civic Arena, Pittsburgh, PA [TTD]\n" +
                        "Sam Holt Band 2018-12-29 MK4+SBD FLAC16\n" +
                        "SAM2018-12-28.SpigsAndMitchThePickleStandDecember2018\n" +
                        "scienceseattle2018-12-29.sbd\n" +
                        "scienceseattle2018-12-29.sbdforreal\n" +
                        "scottsharrard2018-12-22.ca14.flac16\n" +
                        "serenegreen2018-12-31.litz.sbd.schoepsMK41v.flac16\n" +
                        "shb2018-12-29.mk4_sbd.flac16\n" +
                        "SimoneGrazianoSnailspace 20180205pratoFM TTD\n" +
                        "skellogg2018-11-19\n" +
                        "skellogg2018-11-21\n" +
                        "skellogg2018-12-29\n" +
                        "Slaughter 1991-02-27.ECV.DVD9\n" +
                        "sons.of.bill-2018-08-24.hamburg_RC.379a_aud.ca14.m10.flac.16\n" +
                        "Soundgarden 1994-08-05 Hull, QC 3rd GEN - 16 BIT\n" +
                        "southern2018-12-28.flac16\n" +
                        "spafford2018-12-30\n" +
                        "stringcheeseincident2018-12-28.akg481.flac\n" +
                        "Sweating Honey 2012-09-08 Fairbanks, AK Matrix - 16 BIT\n" +
                        "SYF2018-12-22\n" +
                        "TAUK2018-12-29.Tabernacle-24-48\n" +
                        "tb2018-12-29.sbd.flac16.2\n" +
                        "tb2018-12-30\n" +
                        "tfc2018-12-16.CA14card.flacaud\n" +
                        "The Brian Setzer Orchestra - The Granada Theatre, Santa Barbara, CA, 12-18-2017 BACKHAUL 4K \n" +
                        "The Brothers Comatose 2013-02-09 Vancouver CA-11 - 16 BIT\n" +
                        "The Doors 1972-05-03 Beat-Club, DE [TV]\n" +
                        "The Monkees - BBC4 DVB - Making The Monkees [DVD5] (PAL)\n" +
                        "The Rolling Stones_1972-07-25 Both Shows ( Five English Gentlemen Spend A Day In A ) New Yor\n" +
                        "The Tragically Hip 2013-09-12 Vancouver AT943 - 16 BIT\n" +
                        "The Who - 1971-11-29 - Gutter Punks At A Warehouse [SCWH-001]\n" +
                        "The Who - 1980-06-27 - Los Angeles\n" +
                        "thebridge2018-12-30.flac16\n" +
                        "theCAUSE2018-12-30.AmericanBeauty\n" +
                        "TheMightyManatees2018-12-27.SBD.Flac24\n" +
                        "tigermoan2018-12-09aud2448\n" +
                        "Todd_Snider_2018-12-07_Gruene Hall_MK22\n" +
                        "Tom Petty & The Heartbreakers - 1987-07-07 Speedway Track, Weedsport, NY [TTD]\n" +
                        "Tom Petty & The Heartbreakers - 1987-09-10 St. Jakobshalle, Basel, Switzerland [TTD]\n" +
                        "Tom Petty Jacksonville, Fla 1987 july 24 FM flac16\n" +
                        "tr-akus2007-06-23.flac16\n" +
                        "um2018-12-28.ccm4v.flac16\n" +
                        "um2018-12-28.flac16\n" +
                        "UM2018-12-28.NSL\n" +
                        "um2018-12-28_mk41v\n" +
                        "UM2018-12-29.NSL\n" +
                        "UM2018-12-29.Tabernacle-24-48\n" +
                        "um2018-12-29_mk41v\n" +
                        "UM2018-12-30.NSL\n" +
                        "UM2018-12-30.Tabernacle-24-48\n" +
                        "UM2018-12-31.Tabernacle-24-48\n" +
                        "Van Halen 1998-05-16 Rosemont, IL 1st Gen VHS to DVD ntsc krw_co ttd\n" +
                        "wheel2018-12-29.putnamplacematrix\n" +
                        "Widespread Panic 2018-12-30 MK41 FLAC16\n" +
                        "Willie Nelson Carlyles Arroyes CA 9-3-1993 dsdb\n" +
                        "Willie Nile - 2018-12-15 Town Ballroom, Buffalo, NY\n" +
                        "wp2018-12-30.flac\n" +
                        "wp2018-12-31.flac\n" +
                        "wsp2018-12-29-bt\n" +
                        "wsp2018-12-29.flac16\n" +
                        "wsp2018-12-30.flac16\n" +
                        "wsp2018-12-31.flac16\n" +
                        "xmas2018.flac16\n" +
                        "yarn2018-12-30.akg391.flac24\n" +
                        "yes 1991-05-09 McNichols Arena, Denver CO master 44-16\n" +
                        "yes 1991-05-09 McNichols Arena, Denver CO master 96-24\n" +
                        "yes 1994-06-30 Marcus Ampitheater, Milwaukeee WI 44-16\n" +
                        "yes 1994-06-30 Marcus Ampitheater, Milwaukeee WI 96-24\n" +
                        "yes 1994-07-03 Target Center, Mineapolis MN 44-16\n" +
                        "yes 1994-07-03 Target Center, Mineapolis MN 96-24\n" +
                        "yes 1994-07-06 Red Rocks Ampitheatre, Morrison CO master 44-16\n" +
                        "yes 1994-07-06 Red Rocks Ampitheatre, Morrison CO master 96-24\n" +
                        "Yo La Tengo 2013-05-11 Vancouver CA-11 - 16 BIT\n" +
                        "Zendog2018-12-31.SBD.Matrix.Flac16\n";

        final ExtractMetadataFromName extractMetadata = new ExtractMetadataFromName();
        Arrays.asList(names.split("\n")).forEach(name -> System.out.println(extractMetadata.perform(name)));
    }
}
