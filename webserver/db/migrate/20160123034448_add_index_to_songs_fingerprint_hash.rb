class AddIndexToSongsFingerprintHash < ActiveRecord::Migration
  def change
    add_index :songs, :fingerprint_hash, unique: true
  end
end
