
# HikvisionHelper

Companion for Hikvision exported video files.  

Hikvision recorders often produce files with errors during export. This program is designed to fix these issues and also help with concatenating videos.


## Requirements

#### FFmpeg

I suggest using [Chocolatey](https://github.com/chocolatey/choco) for the installation of FFmpeg as it ensures proper configuration of PATH variables.  

Alternatively, you may opt to download it from [FFmpeg](https://github.com/FFmpeg/FFmpeg).
Currently this approach is not advised, because you will have to point to both the "ffmpeg" and "ffprobe" executables at startup.
