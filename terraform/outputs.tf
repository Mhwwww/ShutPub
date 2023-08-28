output "broker_ip" {
  description = "IP Address of Broker"
  value = google_compute_instance.broker.network_interface.0.access_config.0.nat_ip
}

output "publisher_ip" {
  description = "IP Address of Broker"
  value = google_compute_instance.publisher.network_interface.0.access_config.0.nat_ip
}

output "subscriber_ip" {
  description = "IP Address of Broker"
  value = google_compute_instance.subscriber.network_interface.0.access_config.0.nat_ip
}

output "broker_ssh_command" {
  value = "ssh debian@${google_compute_instance.broker.network_interface.0.access_config.0.nat_ip}"
}

output "publisher_ssh_command" {
  value = "ssh debian@${google_compute_instance.publisher.network_interface.0.access_config.0.nat_ip}"
}

output "subscriber_ssh_command" {
  value = "ssh debian@${google_compute_instance.subscriber.network_interface.0.access_config.0.nat_ip}"
}
#sudo sshfs -o allow_other,default_permissions sammy@your_other_server:~/ /mnt/droplet
#output "broker_sshfs_command" {
#  value = "sshfs debian@${google_compute_instance.broker.network_interface.0.access_config.0.nat_ip}:~/ mount_broker/"
#}

