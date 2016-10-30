class CreateSongs < ActiveRecord::Migration
  def change
    create_table :songs do |t|
      t.timestamps null: false
      t.string :url
      t.string :title
      t.string :author
      t.string :artist
      t.text :lyrics
      t.text :fingerprint
      t.integer :fingerprint_hash, limit: 8
      t.text :image
      t.text :mini_image
    end
  end
end
