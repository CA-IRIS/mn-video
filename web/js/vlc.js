function setCam(id){
	var camUrl = 'http://tms-streamer:8080/video/stream?id=' + id + '&size=2';
	doGo(camUrl);
}

function getVLC(name)
{
    if (window.document[name]) 
    {
        return window.document[name];
    }
    if (navigator.appName.indexOf("Microsoft Internet")==-1)
    {
        if (document.embeds && document.embeds[name])
            return document.embeds[name]; 
    }
    else // if (navigator.appName.indexOf("Microsoft Internet")!=-1)
    {
        return document.getElementById(name);
    }
}

function doGo(targetURL)
{
    var vlc = getVLC("vlc");
    vlc.playlist.items.clear();
    while( vlc.playlist.items.count > 0 )
    {
        // clear() may return before the playlist has actually been cleared
        // just wait for it to finish its job
    }
    var itemId = vlc.playlist.add(targetURL, null, null);
    if( itemId != -1 )
    {
        // play MRL
        vlc.playlist.playItem(itemId);
    }
};
function doPlayOrPause()
{
    var vlc = getVLC("vlc");
    if( vlc.playlist.isPlaying )
    {
        vlc.playlist.togglePause();
    }
    else if( vlc.playlist.items.count > 0 )
    {
        vlc.playlist.play();
    }
};
function doStop()
{
    getVLC("vlc").playlist.stop();
    onStop();
};

/* events */

function onOpen()
{
    document.getElementById("PlayOrPause").disabled = true;
    document.getElementById("Stop").disabled = false;
};
function onBuffer()
{
    document.getElementById("PlayOrPause").disabled = true;
    document.getElementById("Stop").disabled = false;
};
function onPlay()
{
    document.getElementById("PlayOrPause").value = "Pause";
    document.getElementById("PlayOrPause").disabled = false;
    document.getElementById("Stop").disabled = false;
};

function onPause()
{
    document.getElementById("PlayOrPause").value = " Play ";
};
function onStop()
{
    var vlc = getVLC("vlc");
    document.getElementById("Stop").disabled = true;
    document.getElementById("PlayOrPause").value = " Play ";
    document.getElementById("PlayOrPause").disabled = false;
};
