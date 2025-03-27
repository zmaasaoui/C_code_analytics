FROM ghcr.io/joernio/joern:nightly

# Install Python dependencies for Neo4j
RUN pip3 install py2neo pandas

# Set working directory for Python scripts
WORKDIR /app

# Copy Python scripts into the container
COPY run_metrics.py metrics.sc neo4j_export.py /app/
COPY ./config/joern-conf.json .
RUN chmod +x /app/run_metrics.py
