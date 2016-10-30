class SongsController < ApplicationController
  before_action :set_song, only: [:show, :edit, :update, :destroy]

  # GET /songs
  # GET /songs.json
  def index
    if (!logged_in?);
      redirect_to login_path
    end
  end

  # GET /songs/1
  # GET /songs/1.json
  def show
    if (!logged_in?);
      redirect_to login_path
    end
  end

  # GET /songs/new
  def new
    if (!logged_in?);
      redirect_to login_path
    end
    @song = Song.new
  end

  # GET /songs/1/edit
  def edit
    if !logged_in?;
      redirect_to login_path
    end
  end

  # POST /songs
  # POST /songs.json
  def create
    if !logged_in?;
      redirect_to login_path
    end

    prs = song_params.permit(:url, :title, :author, :artist, :image, :mini_image, :fingerprint, :fingerprint_hash, :lyrics)

    @song = Song.new(prs)

    respond_to do |format|
      if @song.save
        format.html { redirect_to @song; flash[:success] = 'Song was successfully created.' }
        # format.json { render :show, status: :created, location: @song }
      else
        @errors = @song
        format.html { render :new }
        # format.json { render json: @song.errors, status: :unprocessable_entity }
      end
    end
  end

  # PATCH/PUT /songs/1
  # PATCH/PUT /songs/1.json
  def update
    if !logged_in?;
      redirect_to login_path
    end

    prs = song_params.permit(:url, :title, :author, :artist, :image, :mini_image, :fingerprint, :fingerprint_hash, :lyrics)

    respond_to do |format|
      if @song.update(prs)
        Thread.new do
          result = JSON.parse (detach_song @song.id)
          if result["Verdict"] == "Yes"
            attach_song @song.id, @song.fingerprint
          end
        end
        format.html { sleep 0.5; redirect_to @song; flash[:success] = 'Song was successfully updated.' }
        # format  .json { render :show, status: :ok, location: @song }
      else
        @errors = @song
        format.html { render :edit }
        # format.json { render json: @song.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /songs/1
  # DELETE /songs/1.json
  def destroy
    if !logged_in?;
      return
    end
    @song.destroy
    Thread.new do
      detach_song @song.id
    end

    respond_to do |format|
      format.html { redirect_to songs_url; flash[:success] = 'Song was successfully destroyed.' }
      format.json { head :no_content }
    end
  end

  # GET /block?query=...&offset=...
  def block
    if !logged_in?
      return
    end
    offset = params["offset"]
    limit = 6
    query = params["query"]
    result = Song.limit(limit).offset(offset)
                 .where("#{:title} LIKE ? OR #{:author} LIKE ? OR #{:artist} LIKE ?", "%#{query}%", "%#{query}%", "%#{query}%")
                 .select(:id, :title, :author, :artist, :mini_image)
    if (result.empty?)
      render text: ""
    else
      next_offset = Song.pluck(:id).index(result.last.id) + 1
      render partial: 'songs/element_index', locals: { result: result, offset: next_offset}
    end

  end

  private
  # Use callbacks to share common setup or constraints between actions.
  def set_song
    @song = Song.find(params[:id])
  end

  # Never trust parameters from the scary internet, only allow the white list through.
  def song_params
    params[:song]
  end

end
