class SessionsController < ApplicationController
  def new
    if logged_in?
      redirect_to root_path
    end
  end

  def create
    if (params[:user][:name] == $USER && params[:user][:password] == $PASS)
      flash[:success] = "Hello!"
      login
      redirect_to root_path
    else
      logout
      flash.now[:danger] = "Please try again..."
      render 'new'
    end
  end

  def destroy
    logout
    flash[:success] = "Goodbye!"
    redirect_to login_path
  end

end
