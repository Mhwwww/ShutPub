terraform {
  required_providers {
    google = {
      source = "hashicorp/google"
      version = "4.51.0"
    }
  }
}

#provider "google" {
#  project = "schirmer-project"
#  region = "europe-west3"
#  zone = "europe-west3-a"
#
#  credentials = file(var.key_file_location)
#}
provider "google" {
  project = "silent-scanner-413710"
  region = "europe-west3"
  zone = "europe-west3-a"

  credentials = file(var.key_file_location)
}

#create a Virtual Private Cloud (VPC) network and subnet for the VM's network interface.
resource "google_compute_network" "vpc_network" {
  name = "psf-network"
  auto_create_subnetworks = false
}

// BrokerBaseline VM Config--in frankfurt
resource "google_compute_subnetwork" "default" {
  name          = "psf-broker-subnetwork"
  ip_cidr_range = "10.1.2.0/24"
  network       = google_compute_network.vpc_network.id

#  log_config {
#    aggregation_interval = "INTERVAL_1_MIN"#    INTERVAL_5_SEC. Possible values are: INTERVAL_5_SEC, INTERVAL_30_SEC, INTERVAL_1_MIN, INTERVAL_5_MIN, INTERVAL_10_MIN, INTERVAL_15_MIN.
#    flow_sampling        = 1.0
#    metadata             = "INCLUDE_ALL_METADATA"
#  }
}


resource "google_compute_instance" "broker" {
  name         = "psf-broker-vm"
  machine_type = "e2-standard-2"
  zone = "europe-west3-a"//frankfurt
  tags = ["ssh", "app"]

  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-12"
    }
  }

  metadata = {
    ssh-keys="debian:${file(var.gce_ssh_pub_key_file)}"
  }

  network_interface {
    subnetwork = google_compute_subnetwork.default.id
    access_config {
      # Include this section to give the VM an external IP address
    }
  }
}


// Publisher VM Config--in berlin
#resource "google_compute_subnetwork" "publisher" {
#  name          = "psf-broker-subnetwork"
#  ip_cidr_range = "10.2.2.0/24"
#  region = "europe-west10"
#  network       = google_compute_network.vpc_network.id
#}
resource "google_compute_instance" "publisher" {
  name         = "psf-publisher-vm"
  machine_type = "e2-standard-2"
#  zone = "europe-west10-b"
  tags = ["ssh", "app"]

  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-12"
    }
  }
  metadata = {
    ssh-keys = "debian:${file(var.gce_ssh_pub_key_file)}"
  }

  network_interface {
    subnetwork = google_compute_subnetwork.default.id
    access_config {
      # Include this section to give the VM an external IP address
    }
  }

}

// Subscriber VM Config
#resource "google_compute_subnetwork" "subscriber" {
#  name          = "psf-broker-subnetwork"
#  ip_cidr_range = "10.3.2.0/24"
#  region = "europe-west9"
#  network       = google_compute_network.vpc_network.id
#}
resource "google_compute_instance" "subscriber" {
  name         = "psf-subscriber-vm"
  machine_type = "e2-standard-2"
#  zone = "europe-west9-a"
  tags = ["ssh", "app"]


  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-12"
    }
  }

  metadata = {
    ssh-keys = "debian:${file(var.gce_ssh_pub_key_file)}"
  }

  network_interface {
    subnetwork = google_compute_subnetwork.default.id
    access_config {
      # Include this section to give the VM an external IP address
    }
  }
}




resource "google_compute_firewall" "ssh" {
  name = "allow-ssh"
  allow {
    protocol = "tcp"
    ports = ["22"]
  }
  direction = "INGRESS"
  network = google_compute_network.vpc_network.id
  source_ranges = ["0.0.0.0/0"]
  target_tags = ["ssh"]
}

resource "google_compute_firewall" "grpc_server" {
  name    = "grpc-app-firewall"
  network = google_compute_network.vpc_network.id

  allow {
    protocol = "tcp"
    ports    = ["61616"]
  }
  source_ranges = ["0.0.0.0/0"]
  target_tags = ["app"]
}