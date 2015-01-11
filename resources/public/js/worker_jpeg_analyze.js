(function () {
    importScripts("/js/jpegmeta.js");

    var JpegFile = this.JpegMeta.JpegFile;

    var extractMetadata = function (data) {
        jpeg = new JpegFile(data, "none");

        if (jpeg.exif) {
            return jpeg.exif.DateTimeOriginal.value;
        };

        return null;
    };

    this.addEventListener("message", function (e) {
        var reader = new FileReaderSync();

        var dataURL = reader.readAsDataURL(e.data);
        var o = {
            metadata: extractMetadata(atob(dataURL.replace(/^.*?,/,'')))
        };

        self.postMessage(o);
    });
})();

