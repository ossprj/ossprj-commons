# Commons File

## DirectoryContentHash

A "directory content hash" which can be used to quickly compare the contents of two directories. It is computed as such:

* Find all file paths recursively in a directory (ignores empty directories), relative to the parent directory and append the file length to that relative path
* Remove OS specific file separators 
* Lower case them
* Sort them alphabetically 
* Concatenates them into a string 
* MD5 hash that string


NOTE
* This hash does NOT take into account the contents of the files just the contents of the directory
* Two directories with an identical set of files with identical lengths but different file contents will hash the same

## SearchPath / GetPathsFromSearchPaths

A SearchPath consists of a base path and a search depth.

The base Path will be searched at the specified depth and all directories found at that level will be included in the aggregate List of Path(s)

Only directories will be included.

### Example: Single Top Level Directory
Given the directory structure

    /basePath1/directory1
    /basePath1/directory2
    /basePath1/directory3

A SearchPath with:
* basePath "/basePath1" and searchDepth 1 
  
yields the List of Path(s)

    /basePath1/directory1
    /basePath1/directory2
    /basePath1/directory3

### Example: Single Top Level With Intermediate Directory
Given the directory structure

    /basePath1/volume1/directory1
    /basePath1/volume1/directory2
    /basePath1/volume1/directory3

A SearchPath with:
* basePath "/basePath1" and searchDepth 2
* OR
* basePath "/basePath1/volume1" and searchDepth 1 

yields the List of Path(s)

    /basePath1/volume1/directory1
    /basePath1/volume1/directory2
    /basePath1/volume1/directory3

### Example: Single Top Level With Multiple Intermediate Directories

Given the directory structure

    /basePath1/volume1/partition1/fileset1
    /basePath1/volume1/partition2/fileset2
    /basePath1/volume2/partition1/fileset3

A SearchPath with:
* basePath "/basePath1" and searchDepth 3
* OR SearchPath(s)
* basePath "/basePath1/volume1" and searchDepth 2
* basePath "/basePath1/volume2" and searchDepth 2
* OR SearchPath(s)
* basePath "/basePath1/volume1/partition1" and searchDepth 1
* basePath "/basePath1/volume1/partition2" and searchDepth 1
* basePath "/basePath1/volume2/partition1" and searchDepth 1

Yields the List of Path(s)

    /basePath1/volume1/partition1/fileset1
    /basePath1/volume1/partition2/fileset2
    /basePath1/volume2/partition1/fileset3

### Example: Multiple Top Levels With Multiple Intermediate Directories

Given the directory structure

    /basePath1/volume1/partition1/fileset1
    /basePath1/volume2/partition2/fileset2
    /basePath2/volume3/partition3/fileset3
    /basePath2/volume4/partition4/fileset4

The SearchPath(s):
* basePath "/basePath1" and searchDepth 3
* basePath "/basePath2" and searchDepth 3

Yield the List of Path(s)

    /basePath1/volume1/partition1/fileset1
    /basePath1/volume2/partition2/fileset2
    /basePath2/volume3/partition3/fileset3
    /basePath2/volume4/partition4/fileset4
