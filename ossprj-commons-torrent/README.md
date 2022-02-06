# Commons Torrent

## TorrentContentHash

A hash which can be used to quickly compare the contents of two torrents.

It can also be used to compare the contents of a torrent to a directory via the equivalent [DirectoryContentHash](https://github.com/ossprj/ossprj-commons/tree/main/ossprj-commons-file#directorycontenthash). 

It is computed as such:

* Append the file length to each file path in the torrent (without file separators)
* Lower case them
* Sort them alphabetically
* Concatenate them into a string
* MD5 hash that string

NOTE
* This hash does NOT take into account the contents of the files just the contents of the torrent
* Two torrents with an identical set of files with identical lengths but different file contents will hash the same
