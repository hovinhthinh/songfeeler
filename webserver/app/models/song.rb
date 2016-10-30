class Song < ActiveRecord::Base
  validates :title, presence: true
  validates :fingerprint, presence: true
  validates :fingerprint_hash, presence: true
  validates_uniqueness_of :fingerprint_hash, message: "This song has already been added"
  validates :url, format: { with: /\A((http|https):\/\/[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(([0-9]{1,5})?\/.*)?)?\z/ix }
end
