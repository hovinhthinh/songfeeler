module ApplicationHelper
  def asset_internal_path(asset)
    Rails.application.assets.resolve(asset)
  end
  def asset_external_path(asset)
    ActionController::Base.helpers.asset_path(asset)
  end
end
