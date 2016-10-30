require "uri"
require "net/http"

class ApisController < ApplicationController


  def index
    if !logged_in?;
      redirect_to login_path
    end
    @status = putting_status
    if @status[:message] != ""
      flash.now[:danger] = @status[:message];
    end
  end

  def feel
    if !logged_in?;
      redirect_to login_path
    end
    put_all
    head :no_content
  end

  def status
    if !logged_in?;
      redirect_to login_path
    end
    render json: putting_status
  end

  def analyze
    if !logged_in?;
      redirect_to login_path
    end
    http = Net::HTTP.new $DETECTOR_HOST, $DETECTOR_PORT
    http.use_ssl = false
    request = Net::HTTP::Post.new("/analyzeByUploading/", {'Content-Type' => "application/x-www-form-urlencoded; charset=UTF-8;",
                                                           'Accept' => 'application/json, text/javascript, */*; q=0.01'})
    request.body = params.permit("Type", "Content").to_json
    response = http.request(request)

    render json: response.body
  end

  def contain
    if !logged_in?;
      redirect_to login_path
    end

    http = Net::HTTP.new $DETECTOR_HOST, $DETECTOR_PORT
    http.use_ssl = false
    request = Net::HTTP::Get.new("/contain/?key=" + params["Key"], {'Content-Type' => "application/x-www-form-urlencoded; charset=UTF-8;",
                                                                    'Accept' => 'application/json, text/javascript, */*; q=0.01'})
    response = http.request(request)

    render json: response.body
  end

  def attach
    if !logged_in?;
      redirect_to login_path
    end
    song = Song.find(params["Key"])
    # use helper function
    render json: attach_song(song.id, song.fingerprint)
  end

  def detach
    if !logged_in?;
      redirect_to login_path
    end
    # use helper function
    render json: detach_song(params["Key"])
  end

  def song_info
    song = Song.find(params[:id])
    response = {
        "Title" => song.title,
        "Url" => song.url,
        "Author" => song.author,
        "Artist" => song.artist,
        "Lyrics" => song.lyrics,
        "Image" => song.image
    }.to_json

    render json: response
  end
end
