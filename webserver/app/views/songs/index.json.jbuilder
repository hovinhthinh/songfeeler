json.array!(@songs) do |song|
  json.extract! song, :id,
                :url,
                :title,
                :author,
                :artist,
                :image,
                :mini_image,
                :fingerprint,
                :fingerprint_hash,
                :created_at,
                :updated_at

  json.url song_url(song, format: :json)
end
