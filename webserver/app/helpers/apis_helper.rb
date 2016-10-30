require 'set'

module ApisHelper
  def attach_song(id, fingerprint)
    http = Net::HTTP.new $DETECTOR_HOST, $DETECTOR_PORT
    http.use_ssl = false
    request = Net::HTTP::Post.new("/add/", {'Content-Type' => "application/x-www-form-urlencoded; charset=UTF-8;",
                                            'Accept' => 'application/json, text/javascript, */*; q=0.01'})
    request.body = {"Key" => id, "Fingerprint" => fingerprint}.to_json
    http.request(request).body

  end

  def detach_song(id)
    http = Net::HTTP.new $DETECTOR_HOST, $DETECTOR_PORT
    http.use_ssl = false
    request = Net::HTTP::Get.new("/remove/?key=#{id}", {'Content-Type' => "application/x-www-form-urlencoded; charset=UTF-8;",
                                                             'Accept' => 'application/json, text/javascript, */*; q=0.01'})
    http.request(request).body
  end



  @@putting = nil
  @@mutex_queue = Mutex.new
  @@mutex_put = Mutex.new
  @@mutex_error = Mutex.new
  @@put = 0
  @@error = 0
  @@queue = []
  @@total = 0;
  def put_all
    if @@putting
      return
    end
    @@putting = 1
    begin
      http = Net::HTTP.new $DETECTOR_HOST, $DETECTOR_PORT
      http.use_ssl = false
      request = Net::HTTP::Get.new("/list/", {'Content-Type' => "application/x-www-form-urlencoded; charset=UTF-8;",
                                              'Accept' => 'application/json, text/javascript, */*; q=0.01'})
      attached = Set.new JSON.parse(http.request(request).body)["List"]
      @@put = attached.size
      @@error = 0
      @@queue = []
      current_attached = Song.pluck(:id)
      @@total = current_attached.length
      current_attached.each do |id|
        if !attached.include? id
          @@queue << id
        end
      end
      current_attached = nil

      threads = 4

      threads.times { @@queue << -1 }
      current = 0
      threads.times do
        Thread.new do
          begin
            id = -1
            @@mutex_queue.synchronize do
              id = @@queue[current]; current += 1
            end
            if id != -1
              begin
                song = Song.find id
                attach_song id, song.fingerprint
              rescue
                @@mutex_error.synchronize { @@error += 1}
              else
                @@mutex_put.synchronize { @@put += 1}
              end
            end
          end while id != -1
          if current == @@queue.length; @@putting = nil end
        end
      end

    rescue
      @@put = 0
      @@error = 0
      @@putting = nil
    end
  end

  def putting_status
    if (@@putting != nil)
      {
          status: "Yes",
          info: {
              total: @@total,
              put: @@put,
              error: @@error
          },
          message: ""
      }
    else
      total = 0
      message = ""
      begin
        http = Net::HTTP.new $DETECTOR_HOST, $DETECTOR_PORT
        http.use_ssl = false
        request = Net::HTTP::Get.new("/list/", {'Content-Type' => "application/x-www-form-urlencoded; charset=UTF-8;",
                                                            'Accept' => 'application/json, text/javascript, */*; q=0.01'})
        total = JSON.parse(http.request(request).body)["List"].length
      rescue
        message = "Cannot communicate with detector..."
      end
      {
          status: "No",
          info: {
              total: Song.count,
              put: total,
              error: @@error
          },
          message: message
      }
    end
  end
end
