module SessionsHelper
  def login
    session[:name] = 1
  end

  def logged_in?
    !session[:name].nil?
  end

  def logout
    session[:name] = nil
  end
  
end